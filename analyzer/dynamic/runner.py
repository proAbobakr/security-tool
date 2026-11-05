"""
Dynamic analysis orchestrator.

Coordinates the optional runtime components (Frida instrumentation, network
capture, logcat scanning) against an app on a connected device, then merges
their output into a single findings structure compatible with the existing
Reporter. Intended for authorized assessment of apps you own or may test.

Any component whose external tool is unavailable is skipped with a recorded,
non-fatal error, so a partial environment still yields a useful report.
"""

import os
from dataclasses import dataclass, field
from typing import Any, Callable, Dict, List, Optional

from . import patterns, traffic
from .device import AdbClient, DeviceError
from .instrumentation import FridaInstrumentation, InstrumentationError
from .logcat import LogcatMonitor


@dataclass
class DynamicConfig:
    package: str
    serial: Optional[str] = None
    duration: float = 20.0
    spawn: bool = True
    enable_frida: bool = True
    enable_traffic: bool = False
    enable_logcat: bool = True
    proxy_port: int = 8080
    apk_path: Optional[str] = None          # if set, install before running
    traffic_out: Optional[str] = None       # JSONL path for live capture
    har_path: Optional[str] = None          # offline traffic analysis
    flows_path: Optional[str] = None        # offline JSONL analysis
    adb_path: Optional[str] = None


class DynamicAnalyzer:
    def __init__(self, config: DynamicConfig):
        self.config = config
        self.findings: List[Dict[str, Any]] = []
        self.component_errors: Dict[str, str] = {}
        self.components_run: List[str] = []

    def analyze(self, on_event: Optional[Callable[[dict], None]] = None) -> Dict[str, Any]:
        cfg = self.config
        metadata: Dict[str, Any] = {"package": cfg.package, "duration": cfg.duration}

        adb = AdbClient(serial=cfg.serial, adb_path=cfg.adb_path)
        device_ready = self._prepare_device(adb, metadata)

        if device_ready and cfg.apk_path:
            self._install_apk(adb)

        # Live traffic capture wraps the instrumentation window.
        capture = self._maybe_start_capture()
        try:
            if cfg.enable_frida and device_ready:
                self._run_frida(on_event)
        finally:
            if capture is not None:
                capture.__exit__(None, None, None)
                self.components_run.append("traffic")
                self._analyze_live_traffic(capture)

        if cfg.enable_logcat and device_ready:
            self._run_logcat(adb)

        # Offline traffic sources can be analyzed with no device at all.
        self._analyze_offline_traffic()

        metadata["components_run"] = self.components_run
        metadata["component_errors"] = self.component_errors
        return {
            "analysis_type": "dynamic",
            "file_path": cfg.apk_path or cfg.package,
            "file_type": "LIVE",
            "package": cfg.package,
            "findings": self.findings,
            "metadata": metadata,
        }

    # -- device -----------------------------------------------------------
    def _prepare_device(self, adb: AdbClient, metadata: Dict[str, Any]) -> bool:
        try:
            info = adb.ensure_device()
            metadata["device"] = {
                "serial": info.serial,
                "model": info.model,
                "android_version": adb.get_prop("ro.build.version.release") or None,
            }
            return True
        except DeviceError as exc:
            self.component_errors["device"] = str(exc)
            metadata["device"] = None
            return False

    def _install_apk(self, adb: AdbClient) -> None:
        try:
            adb.install(self.config.apk_path)
            self.components_run.append("install")
        except DeviceError as exc:
            self.component_errors["install"] = str(exc)

    # -- frida ------------------------------------------------------------
    def _run_frida(self, on_event: Optional[Callable[[dict], None]]) -> None:
        cfg = self.config
        try:
            instr = FridaInstrumentation(
                package=cfg.package, serial=cfg.serial, spawn=cfg.spawn,
            )
            result = instr.run(duration=cfg.duration, on_event=on_event)
            self.components_run.append("frida")
            self.findings.extend(self._normalize_frida_findings(result.findings))
            self.findings.extend(self._findings_from_observations(result.observations))
            for err in result.errors:
                self.component_errors.setdefault("frida_hooks", err)
        except InstrumentationError as exc:
            self.component_errors["frida"] = str(exc)

    def _normalize_frida_findings(self, raw: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        out: List[Dict[str, Any]] = []
        seen = set()
        for f in raw:
            key = (f.get("check"), f.get("detail"))
            if key in seen:
                continue
            seen.add(key)
            out.append({
                "severity": f.get("severity", "MEDIUM"),
                "title": f.get("title", "Runtime finding"),
                "description": f.get("detail", ""),
                "owasp_category": f.get("owasp", ""),
                "source": "frida",
            })
        return out

    def _findings_from_observations(self, obs: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        findings: List[Dict[str, Any]] = []
        insecure_random = False
        debug_logging = False
        raw_sql = 0

        for o in obs:
            channel = o.get("channel")
            data = o.get("data", {})
            if channel == "insecure-random":
                insecure_random = True
            elif channel == "webview" and data.get("value") is True:
                findings.append({
                    "severity": "LOW",
                    "title": "WebView JavaScript enabled at runtime",
                    "description": "WebSettings.setJavaScriptEnabled(true) was called. "
                                   "Ensure loaded content is trusted and input validated.",
                    "owasp_category": "M1: Improper Platform Usage",
                    "source": "frida",
                })
            elif channel == "raw-sql":
                raw_sql += 1
            elif channel == "log":
                debug_logging = True
                for hit in patterns.scan_text(str(data.get("message", ""))):
                    findings.append({
                        "severity": hit["severity"],
                        "title": f"Sensitive data logged by app ({hit['category']})",
                        "description": f"android.util.Log.{data.get('level')} emitted a "
                                       f"value resembling {hit['category']} "
                                       f"(excerpt: {hit['excerpt']}).",
                        "owasp_category": "M7: Client Code Quality",
                        "cwe": "CWE-532",
                        "source": "frida",
                    })

        if insecure_random:
            findings.append({
                "severity": "MEDIUM",
                "title": "java.util.Random used",
                "description": "java.util.Random was constructed at runtime. If used for "
                               "security tokens/keys, replace with java.security.SecureRandom.",
                "owasp_category": "M5: Insufficient Cryptography",
                "source": "frida",
            })
        if debug_logging:
            findings.append({
                "severity": "LOW",
                "title": "Debug-level logging active",
                "description": "The app emitted debug/verbose logcat entries at runtime. "
                               "Strip debug logging from release builds.",
                "owasp_category": "M7: Client Code Quality",
                "source": "frida",
            })
        if raw_sql:
            findings.append({
                "severity": "LOW",
                "title": f"Raw SQL executed ({raw_sql}x)",
                "description": "SQLiteDatabase.execSQL was called with raw SQL. Verify all "
                               "user-influenced queries use parameter binding.",
                "owasp_category": "M7: Client Code Quality",
                "source": "frida",
            })
        return findings

    # -- traffic ----------------------------------------------------------
    def _maybe_start_capture(self) -> Optional[traffic.TrafficCapture]:
        cfg = self.config
        if not cfg.enable_traffic:
            return None
        out = cfg.traffic_out or "flows.jsonl"
        try:
            cap = traffic.TrafficCapture(out_path=out, port=cfg.proxy_port)
            return cap.__enter__()
        except traffic.TrafficError as exc:
            self.component_errors["traffic"] = str(exc)
            return None

    def _analyze_live_traffic(self, capture: traffic.TrafficCapture) -> None:
        try:
            self.findings.extend(traffic.analyze_flows(capture.load()))
        except Exception as exc:  # analysis must never abort the run
            self.component_errors.setdefault("traffic_analysis", str(exc))

    def _analyze_offline_traffic(self) -> None:
        cfg = self.config
        if cfg.har_path and os.path.isfile(cfg.har_path):
            try:
                self.findings.extend(traffic.analyze_flows(traffic.load_har(cfg.har_path)))
                self.components_run.append("har")
            except Exception as exc:
                self.component_errors["har"] = str(exc)
        if cfg.flows_path and os.path.isfile(cfg.flows_path):
            try:
                self.findings.extend(traffic.analyze_flows(traffic.load_jsonl(cfg.flows_path)))
                self.components_run.append("flows")
            except Exception as exc:
                self.component_errors["flows"] = str(exc)

    # -- logcat -----------------------------------------------------------
    def _run_logcat(self, adb: AdbClient) -> None:
        try:
            self.findings.extend(LogcatMonitor(adb).scan())
            self.components_run.append("logcat")
        except DeviceError as exc:
            self.component_errors["logcat"] = str(exc)
