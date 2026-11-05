"""
Logcat sensitive-data scanner.

Release builds should not emit sensitive data to the system log, which is
readable by other processes with READ_LOGS or via adb during assessment.
This module scans logcat output (captured live via adb or from a saved file)
for values that should never be logged.
"""

import os
from dataclasses import dataclass
from typing import Any, Dict, Iterable, List, Optional

from . import patterns
from .device import AdbClient, DeviceError


@dataclass
class LogHit:
    line_no: int
    category: str
    severity: str
    excerpt: str
    line_excerpt: str


def scan_lines(lines: Iterable[str]) -> List[LogHit]:
    """Scan logcat lines for sensitive values. Pure and testable."""
    hits: List[LogHit] = []
    for i, line in enumerate(lines, 1):
        for match in patterns.scan_text(line):
            hits.append(LogHit(
                line_no=i,
                category=match["category"],
                severity=match["severity"],
                excerpt=match["excerpt"],
                line_excerpt=line.strip()[:160],
            ))
    return hits


def hits_to_findings(hits: List[LogHit]) -> List[Dict[str, Any]]:
    """Convert scan hits into de-duplicated OWASP findings (M7)."""
    findings: List[Dict[str, Any]] = []
    seen = set()
    for hit in hits:
        if hit.category in seen:
            continue
        seen.add(hit.category)
        findings.append({
            "severity": hit.severity,
            "title": f"Sensitive data in logs ({hit.category})",
            "description": (
                f"A value resembling {hit.category} was written to logcat "
                f"(excerpt: {hit.excerpt}). Remove sensitive logging from "
                f"release builds."
            ),
            "owasp_category": "M7: Client Code Quality",
            "cwe": "CWE-532",
            "source": "logcat",
        })
    return findings


def scan_file(path: str) -> List[Dict[str, Any]]:
    """Scan a saved logcat dump file and return findings."""
    with open(path, "r", encoding="utf-8", errors="replace") as fh:
        return hits_to_findings(scan_lines(fh))


class LogcatMonitor:
    """Capture logcat via adb and scan it for sensitive data."""

    def __init__(self, adb: AdbClient):
        self.adb = adb

    def capture_dump(self, clear_first: bool = True) -> List[str]:
        """Return the current logcat buffer as a list of lines."""
        if clear_first:
            self.adb.logcat_clear()
        proc = self.adb.run(["logcat", "-d", "-v", "time"], check=False)
        if proc.returncode != 0:
            raise DeviceError(f"logcat dump failed: {proc.stderr.strip()}")
        return proc.stdout.splitlines()

    def scan(self, clear_first: bool = False) -> List[Dict[str, Any]]:
        return hits_to_findings(scan_lines(self.capture_dump(clear_first)))
