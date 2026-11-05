"""
Network traffic capture and analysis.

Capture (optional) launches `mitmdump` as a subprocess with the bundled
flow_logger addon; the device must be configured to route through the proxy
and trust its CA for TLS interception (standard MASTG setup). Analysis is
pure Python and runs on the resulting JSONL -- or on any standard HAR export,
so traffic can be assessed without a live device.
"""

import json
import os
import shutil
import signal
import subprocess
from dataclasses import dataclass, field
from typing import Any, Dict, List, Optional
from urllib.parse import urlparse

from . import patterns

_ADDON = os.path.join(os.path.dirname(__file__), "addons", "flow_logger.py")


class TrafficError(RuntimeError):
    """Raised when traffic capture cannot start."""


@dataclass
class FlowRecord:
    scheme: str = ""
    method: str = "GET"
    host: str = ""
    path: str = "/"
    url: str = ""
    req_headers: Dict[str, str] = field(default_factory=dict)
    req_body: str = ""
    status: Optional[int] = None
    resp_headers: Dict[str, str] = field(default_factory=dict)
    resp_body: str = ""


class TrafficCapture:
    """Context manager that runs mitmdump and captures flows to JSONL."""

    def __init__(self, out_path: str, port: int = 8080,
                 mitmdump_path: Optional[str] = None, extra_args: Optional[List[str]] = None):
        self.out_path = out_path
        self.port = port
        self.mitmdump_path = mitmdump_path or shutil.which("mitmdump")
        self.extra_args = extra_args or []
        self._proc: Optional[subprocess.Popen] = None

    @property
    def available(self) -> bool:
        return self.mitmdump_path is not None

    def __enter__(self) -> "TrafficCapture":
        if not self.available:
            raise TrafficError(
                "mitmdump not found. Install mitmproxy (`pip install mitmproxy`) "
                "to capture live traffic, or analyze an existing HAR/JSONL file."
            )
        # Fresh log each run so analysis isn't polluted by prior captures.
        if os.path.exists(self.out_path):
            os.remove(self.out_path)
        cmd = [
            self.mitmdump_path, "-q", "-s", _ADDON,
            "--set", f"flow_out={self.out_path}",
            "--listen-port", str(self.port),
        ] + self.extra_args
        try:
            self._proc = subprocess.Popen(
                cmd, stdout=subprocess.DEVNULL, stderr=subprocess.PIPE, text=True,
            )
        except OSError as exc:
            raise TrafficError(f"failed to start mitmdump: {exc}") from exc
        return self

    def __exit__(self, exc_type, exc, tb) -> None:
        if self._proc is None:
            return
        self._proc.send_signal(signal.SIGINT)
        try:
            self._proc.wait(timeout=10)
        except subprocess.TimeoutExpired:
            self._proc.kill()

    def load(self) -> List[FlowRecord]:
        return load_jsonl(self.out_path)


def load_jsonl(path: str) -> List[FlowRecord]:
    """Load flow records produced by the flow_logger addon."""
    records: List[FlowRecord] = []
    if not os.path.isfile(path):
        return records
    with open(path, "r", encoding="utf-8") as fh:
        for line in fh:
            line = line.strip()
            if not line:
                continue
            try:
                d = json.loads(line)
            except json.JSONDecodeError:
                continue
            records.append(FlowRecord(
                scheme=d.get("scheme", ""), method=d.get("method", "GET"),
                host=d.get("host", ""), path=d.get("path", "/"),
                url=d.get("url", ""), req_headers=d.get("req_headers", {}) or {},
                req_body=d.get("req_body", "") or "", status=d.get("status"),
                resp_headers=d.get("resp_headers", {}) or {},
                resp_body=d.get("resp_body", "") or "",
            ))
    return records


def load_har(path: str) -> List[FlowRecord]:
    """Load flow records from a standard HAR (HTTP Archive) export."""
    with open(path, "r", encoding="utf-8") as fh:
        har = json.load(fh)
    records: List[FlowRecord] = []
    for entry in har.get("log", {}).get("entries", []):
        req = entry.get("request", {})
        resp = entry.get("response", {})
        url = req.get("url", "")
        parsed = urlparse(url)
        records.append(FlowRecord(
            scheme=parsed.scheme, method=req.get("method", "GET"),
            host=parsed.hostname or "", path=parsed.path or "/", url=url,
            req_headers={h.get("name", ""): h.get("value", "")
                         for h in req.get("headers", [])},
            req_body=(req.get("postData", {}) or {}).get("text", "") or "",
            status=resp.get("status"),
            resp_headers={h.get("name", ""): h.get("value", "")
                          for h in resp.get("headers", [])},
            resp_body=(resp.get("content", {}) or {}).get("text", "") or "",
        ))
    return records


def _headers_lower(headers: Dict[str, str]) -> Dict[str, str]:
    return {k.lower(): v for k, v in headers.items()}


def analyze_flows(records: List[FlowRecord]) -> List[Dict[str, Any]]:
    """
    Inspect captured flows and return OWASP-tagged findings.

    Checks: cleartext HTTP (M3), sensitive data in URLs (M3/M9), sensitive
    data in request/response bodies and headers (M3), and missing HSTS on
    HTTPS responses (M3). Findings are de-duplicated per (check, host).
    """
    findings: List[Dict[str, Any]] = []
    seen = set()

    def emit(check, severity, title, detail, host):
        key = (check, host)
        if key in seen:
            return
        seen.add(key)
        findings.append({
            "severity": severity,
            "title": title,
            "description": detail,
            "owasp_category": "M3: Insecure Communication",
            "source": "network",
            "host": host,
        })

    for rec in records:
        host = rec.host or (urlparse(rec.url).hostname or "")

        if rec.scheme == "http":
            emit("cleartext_http", "HIGH",
                 "Cleartext HTTP traffic observed",
                 f"App sent an HTTP request to {host}{rec.path}. Traffic is "
                 f"unencrypted and interceptable.", host)

        for hit in patterns.scan_text(rec.url):
            emit(f"url_leak_{hit['category']}", _bump(hit["severity"]),
                 f"Sensitive data in URL ({hit['category']})",
                 f"A value resembling {hit['category']} appears in a request URL "
                 f"to {host} (excerpt: {hit['excerpt']}). URLs are logged by "
                 f"proxies and servers.", host)

        body_blob = "\n".join([rec.req_body, rec.resp_body])
        for hit in patterns.scan_text(body_blob):
            sev = hit["severity"] if rec.scheme == "https" else _bump(hit["severity"])
            emit(f"body_leak_{hit['category']}", sev,
                 f"Sensitive data in traffic body ({hit['category']})",
                 f"A value resembling {hit['category']} was seen in a "
                 f"{rec.scheme.upper()} body to/from {host} "
                 f"(excerpt: {hit['excerpt']}).", host)

        header_blob = "\n".join(f"{k}: {v}" for k, v in
                                {**rec.req_headers, **rec.resp_headers}.items())
        for hit in patterns.scan_text(header_blob):
            if hit["category"] in ("Bearer Token", "Basic Auth Header"):
                continue  # expected in Authorization headers; not a leak by itself
            emit(f"header_leak_{hit['category']}", hit["severity"],
                 f"Sensitive data in headers ({hit['category']})",
                 f"A value resembling {hit['category']} appears in headers for "
                 f"{host} (excerpt: {hit['excerpt']}).", host)

        if rec.scheme == "https" and rec.status:
            if "strict-transport-security" not in _headers_lower(rec.resp_headers):
                emit("missing_hsts", "LOW",
                     "Missing HSTS header",
                     f"HTTPS response from {host} lacks Strict-Transport-Security, "
                     f"allowing protocol downgrade.", host)

    return findings


def _bump(severity: str) -> str:
    """Raise severity one level (cleartext exposure is worse)."""
    order = ["INFO", "LOW", "MEDIUM", "HIGH", "CRITICAL"]
    try:
        return order[min(order.index(severity) + 1, len(order) - 1)]
    except ValueError:
        return severity
