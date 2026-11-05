"""
Frida-based runtime instrumentation.

Loads the observer agent into a target app and collects the security findings
and observations it emits. The Java bridge is no longer bundled with Frida 17+,
so the agent is compiled with frida-java-bridge resolved from a local
node_modules. To keep startup fast and offline-friendly, a precompiled bundle
is used when present; otherwise it is built on demand and cached.

Everything here degrades gracefully: if frida is not installed, or no device /
frida-server is reachable, start() raises InstrumentationError with guidance
rather than crashing the whole analysis.
"""

import os
import queue
import shutil
import subprocess
import threading
from dataclasses import dataclass, field
from typing import Any, Callable, Dict, List, Optional

try:
    import frida
    FRIDA_AVAILABLE = True
except ImportError:  # pragma: no cover - exercised only without frida
    frida = None
    FRIDA_AVAILABLE = False

_AGENT_DIR = os.path.join(os.path.dirname(__file__), "agents")
_AGENT_SRC = os.path.join(_AGENT_DIR, "observer.js")
_AGENT_BUNDLE = os.path.join(_AGENT_DIR, "observer.bundle.js")
_BRIDGE_SPEC = "frida-java-bridge"


class InstrumentationError(RuntimeError):
    """Raised when the app cannot be instrumented."""


@dataclass
class InstrumentationResult:
    findings: List[Dict[str, Any]] = field(default_factory=list)
    observations: List[Dict[str, Any]] = field(default_factory=list)
    errors: List[str] = field(default_factory=list)
    ready: bool = False


def ensure_agent_bundle(agent_dir: str = _AGENT_DIR,
                        rebuild: bool = False) -> str:
    """
    Return a path to a compiled agent bundle, building it if necessary.

    Resolution order:
      1. An existing observer.bundle.js (unless rebuild=True).
      2. Compile observer.js with frida.Compiler, resolving frida-java-bridge
         from agent_dir/node_modules (installing it first if absent).
    """
    if not rebuild and os.path.isfile(_AGENT_BUNDLE):
        return _AGENT_BUNDLE

    if not FRIDA_AVAILABLE:
        raise InstrumentationError(
            "frida is not installed; cannot compile the agent. "
            "`pip install frida` or ship a precompiled observer.bundle.js."
        )

    _ensure_java_bridge(agent_dir)

    compiler = frida.Compiler()
    diagnostics: List[Any] = []
    compiler.on("diagnostics", lambda d: diagnostics.append(d))
    try:
        bundle = compiler.build("observer.js", project_root=agent_dir)
    except Exception as exc:  # frida raises its own error types
        raise InstrumentationError(
            f"failed to compile observer agent: {exc}. diagnostics={diagnostics}"
        ) from exc

    with open(_AGENT_BUNDLE, "w", encoding="utf-8") as fh:
        fh.write(bundle)
    return _AGENT_BUNDLE


def _ensure_java_bridge(agent_dir: str) -> None:
    """Make frida-java-bridge resolvable under agent_dir/node_modules."""
    bridge_dir = os.path.join(agent_dir, "node_modules", _BRIDGE_SPEC)
    if os.path.isdir(bridge_dir):
        return

    # Preferred: Frida's own package manager (no Node toolchain needed).
    try:
        frida.PackageManager().install(project_root=agent_dir, specs=[_BRIDGE_SPEC])
        if os.path.isdir(bridge_dir):
            return
    except Exception:
        pass  # fall back to npm

    npm = shutil.which("npm")
    if npm is None:
        raise InstrumentationError(
            f"{_BRIDGE_SPEC} is not installed and neither Frida's package "
            f"manager nor npm is available. Install it manually: "
            f"`cd {agent_dir} && npm install {_BRIDGE_SPEC}`."
        )
    proc = subprocess.run(
        [npm, "install", "--no-audit", "--no-fund", _BRIDGE_SPEC],
        cwd=agent_dir, capture_output=True, text=True, timeout=300,
    )
    if proc.returncode != 0 or not os.path.isdir(bridge_dir):
        raise InstrumentationError(
            f"npm install {_BRIDGE_SPEC} failed: "
            f"{proc.stderr.strip() or proc.stdout.strip()}"
        )


class FridaInstrumentation:
    """Drives a single instrumentation run against one app."""

    def __init__(self, package: str, serial: Optional[str] = None,
                 spawn: bool = True, agent_dir: str = _AGENT_DIR):
        self.package = package
        self.serial = serial
        self.spawn = spawn
        self.agent_dir = agent_dir
        self._result = InstrumentationResult()
        self._q: "queue.Queue[dict]" = queue.Queue()

    def _get_device(self):
        if self.serial:
            return frida.get_device(self.serial, timeout=10)
        return frida.get_usb_device(timeout=10)

    def _on_message(self, message: Dict[str, Any], _data) -> None:
        # Frida wraps agent send() payloads as {'type':'send','payload':...}.
        if message.get("type") == "error":
            self._q.put({"kind": "hook-error",
                         "error": message.get("description", "script error")})
            return
        payload = message.get("payload")
        if isinstance(payload, dict):
            self._q.put(payload)

    def run(self, duration: float = 20.0,
            on_event: Optional[Callable[[dict], None]] = None) -> InstrumentationResult:
        """
        Instrument the app for `duration` seconds and return collected results.

        on_event, if given, is called for each payload as it arrives (used for
        live log streaming by the orchestrator).
        """
        if not FRIDA_AVAILABLE:
            raise InstrumentationError(
                "frida is not installed. `pip install frida` and ensure a "
                "frida-server matching your frida version runs on the device."
            )

        bundle_path = ensure_agent_bundle(self.agent_dir)
        with open(bundle_path, "r", encoding="utf-8") as fh:
            source = fh.read()

        try:
            device = self._get_device()
        except Exception as exc:
            raise InstrumentationError(
                f"no Frida device reachable: {exc}. Start frida-server on the "
                f"device/emulator and confirm `frida-ps -U` works."
            ) from exc

        pid = None
        session = None
        try:
            if self.spawn:
                pid = device.spawn([self.package])
                session = device.attach(pid)
            else:
                session = device.attach(self.package)

            script = session.create_script(source)
            script.on("message", self._on_message)
            script.load()

            exports = getattr(script, "exports_sync", None) or script.exports
            exports.start()

            if pid is not None:
                device.resume(pid)

            self._drain(duration, on_event)
        except InstrumentationError:
            raise
        except Exception as exc:
            raise InstrumentationError(f"instrumentation failed: {exc}") from exc
        finally:
            if session is not None:
                try:
                    session.detach()
                except Exception:
                    pass

        return self._result

    def _drain(self, duration: float,
               on_event: Optional[Callable[[dict], None]]) -> None:
        """Collect agent messages for the given wall-clock duration."""
        deadline = threading.Event()
        timer = threading.Timer(duration, deadline.set)
        timer.daemon = True
        timer.start()
        try:
            while not deadline.is_set():
                try:
                    payload = self._q.get(timeout=0.25)
                except queue.Empty:
                    continue
                self._dispatch(payload, on_event)
            # Flush anything already queued.
            while True:
                try:
                    self._dispatch(self._q.get_nowait(), on_event)
                except queue.Empty:
                    break
        finally:
            timer.cancel()

    def _dispatch(self, payload: Dict[str, Any],
                  on_event: Optional[Callable[[dict], None]]) -> None:
        kind = payload.get("kind")
        if kind == "finding":
            self._result.findings.append(payload)
        elif kind == "observation":
            self._result.observations.append(payload)
        elif kind == "ready":
            self._result.ready = True
        elif kind in ("hook-error",):
            self._result.errors.append(payload.get("error", "unknown hook error"))
        if on_event is not None:
            on_event(payload)
