"""
Thin wrapper around the Android Debug Bridge (adb).

Only the operations the dynamic analyzer needs are exposed. Every method
raises DeviceError with an actionable message when adb is missing or a
command fails, so callers can degrade gracefully instead of crashing.
"""

import shutil
import subprocess
from dataclasses import dataclass
from typing import List, Optional


class DeviceError(RuntimeError):
    """Raised when an adb operation cannot be completed."""


@dataclass
class DeviceInfo:
    serial: str
    state: str
    model: Optional[str] = None
    android_version: Optional[str] = None


class AdbClient:
    """Minimal adb client scoped to a single target device."""

    def __init__(self, serial: Optional[str] = None, adb_path: Optional[str] = None,
                 timeout: int = 60):
        self.adb_path = adb_path or shutil.which("adb")
        self.serial = serial
        self.timeout = timeout

    @property
    def available(self) -> bool:
        return self.adb_path is not None

    def _base_cmd(self) -> List[str]:
        if not self.available:
            raise DeviceError(
                "adb not found on PATH. Install Android platform-tools and ensure "
                "`adb` is runnable, or pass --adb-path."
            )
        cmd = [self.adb_path]
        if self.serial:
            cmd += ["-s", self.serial]
        return cmd

    def run(self, args: List[str], check: bool = True,
            timeout: Optional[int] = None) -> subprocess.CompletedProcess:
        """Run an adb subcommand and return the completed process."""
        cmd = self._base_cmd() + args
        try:
            proc = subprocess.run(
                cmd, capture_output=True, text=True,
                timeout=timeout or self.timeout,
            )
        except subprocess.TimeoutExpired as exc:
            raise DeviceError(f"adb command timed out: {' '.join(args)}") from exc
        except OSError as exc:
            raise DeviceError(f"failed to execute adb: {exc}") from exc

        if check and proc.returncode != 0:
            raise DeviceError(
                f"adb {' '.join(args)} failed (exit {proc.returncode}): "
                f"{proc.stderr.strip() or proc.stdout.strip()}"
            )
        return proc

    def list_devices(self) -> List[DeviceInfo]:
        """Return attached devices (state 'device', 'offline', etc.)."""
        proc = self.run(["devices", "-l"], check=True)
        devices: List[DeviceInfo] = []
        for line in proc.stdout.splitlines()[1:]:
            line = line.strip()
            if not line:
                continue
            parts = line.split()
            serial, state = parts[0], parts[1]
            model = next((p.split(":", 1)[1] for p in parts[2:]
                          if p.startswith("model:")), None)
            devices.append(DeviceInfo(serial=serial, state=state, model=model))
        return devices

    def ensure_device(self) -> DeviceInfo:
        """Return the single ready device, resolving/validating the serial."""
        ready = [d for d in self.list_devices() if d.state == "device"]
        if not ready:
            raise DeviceError(
                "no device in 'device' state. Connect a device/emulator and "
                "authorize USB debugging (`adb devices`)."
            )
        if self.serial:
            match = next((d for d in ready if d.serial == self.serial), None)
            if match is None:
                raise DeviceError(f"requested device {self.serial!r} is not ready.")
            return match
        if len(ready) > 1:
            serials = ", ".join(d.serial for d in ready)
            raise DeviceError(f"multiple devices attached ({serials}); pass --serial.")
        self.serial = ready[0].serial
        return ready[0]

    def get_prop(self, prop: str) -> str:
        return self.run(["shell", "getprop", prop], check=False).stdout.strip()

    def install(self, apk_path: str, reinstall: bool = True) -> None:
        args = ["install"]
        if reinstall:
            args.append("-r")
        args.append(apk_path)
        self.run(args, check=True, timeout=max(self.timeout, 180))

    def uninstall(self, package: str) -> None:
        self.run(["uninstall", package], check=False)

    def is_installed(self, package: str) -> bool:
        proc = self.run(["shell", "pm", "list", "packages", package], check=False)
        return any(line.strip() == f"package:{package}"
                   for line in proc.stdout.splitlines())

    def force_stop(self, package: str) -> None:
        self.run(["shell", "am", "force-stop", package], check=False)

    def start_app(self, package: str) -> None:
        """Launch an app's default launcher activity via monkey."""
        self.run(
            ["shell", "monkey", "-p", package,
             "-c", "android.intent.category.LAUNCHER", "1"],
            check=False,
        )

    def pull(self, remote: str, local: str) -> None:
        self.run(["pull", remote, local], check=True)

    def logcat_clear(self) -> None:
        self.run(["logcat", "-c"], check=False)

    def is_rooted(self) -> bool:
        """Best-effort check for a usable su (needed for storage inspection)."""
        proc = self.run(["shell", "su", "-c", "id"], check=False, timeout=10)
        return proc.returncode == 0 and "uid=0" in proc.stdout
