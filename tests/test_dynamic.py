"""
Unit tests for the pure-Python dynamic-analysis logic.

These avoid external tools (adb, frida, mitmproxy, a device) by exercising the
parsing/scanning/aggregation code directly and faking adb output where needed.
"""

import json
import os
import subprocess
import sys

import pytest

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from analyzer.dynamic import patterns
from analyzer.dynamic import traffic
from analyzer.dynamic import logcat
from analyzer.dynamic.device import AdbClient, DeviceError
from analyzer.dynamic.runner import DynamicAnalyzer, DynamicConfig


# -- patterns --------------------------------------------------------------
def test_scan_text_detects_aws_and_secret_keyvalue():
    text = "config AKIAIOSFODNN7EXAMPLE and password = hunter2plaintext"
    cats = {h["category"] for h in patterns.scan_text(text)}
    assert "AWS Access Key ID" in cats
    assert "Password" in cats


def test_scan_text_redacts_match():
    hits = patterns.scan_text("AKIAIOSFODNN7EXAMPLE")
    excerpt = hits[0]["excerpt"]
    assert excerpt.startswith("AKIA")
    assert "*" in excerpt
    assert "IOSFODNN7EXAMPLE" not in excerpt


def test_scan_text_empty():
    assert patterns.scan_text("") == []


# -- traffic ---------------------------------------------------------------
def _rec(**kw):
    return traffic.FlowRecord(**kw)


def test_cleartext_http_flagged_high():
    findings = traffic.analyze_flows([_rec(scheme="http", host="api.example.com", path="/v1")])
    assert any(f["title"].startswith("Cleartext HTTP") and f["severity"] == "HIGH"
               for f in findings)


def test_sensitive_body_over_https_is_reported():
    findings = traffic.analyze_flows([_rec(
        scheme="https", host="api.example.com", status=200,
        resp_headers={"Strict-Transport-Security": "max-age=1"},
        resp_body='{"ssn":"123-45-6789"}',
    )])
    assert any("US SSN" in f["title"] for f in findings)


def test_missing_hsts_flagged_on_https():
    findings = traffic.analyze_flows([_rec(
        scheme="https", host="h.example.com", status=200, resp_headers={},
    )])
    assert any(f["title"] == "Missing HSTS header" for f in findings)


def test_authorization_header_not_flagged_as_leak():
    findings = traffic.analyze_flows([_rec(
        scheme="https", host="h.example.com", status=200,
        req_headers={"Authorization": "Bearer abcdefghijklmnopqrstuvwxyz"},
        resp_headers={"Strict-Transport-Security": "max-age=1"},
    )])
    assert not any("headers" in f["title"] for f in findings)


def test_findings_deduped_per_host():
    recs = [_rec(scheme="http", host="dup.example.com", path="/a"),
            _rec(scheme="http", host="dup.example.com", path="/b")]
    cleartext = [f for f in traffic.analyze_flows(recs)
                 if f["title"].startswith("Cleartext HTTP")]
    assert len(cleartext) == 1


def test_load_har_roundtrip(tmp_path):
    har = {"log": {"entries": [{
        "request": {"method": "GET", "url": "http://plain.example.com/path",
                    "headers": [{"name": "Host", "value": "plain.example.com"}]},
        "response": {"status": 200, "headers": [],
                     "content": {"text": "ok"}},
    }]}}
    p = tmp_path / "t.har"
    p.write_text(json.dumps(har))
    recs = traffic.load_har(str(p))
    assert recs[0].scheme == "http" and recs[0].host == "plain.example.com"
    assert any(f["title"].startswith("Cleartext HTTP")
               for f in traffic.analyze_flows(recs))


def test_load_jsonl_skips_bad_lines(tmp_path):
    p = tmp_path / "flows.jsonl"
    p.write_text('{"scheme":"http","host":"x"}\nnot-json\n')
    recs = traffic.load_jsonl(str(p))
    assert len(recs) == 1 and recs[0].host == "x"


# -- logcat ----------------------------------------------------------------
def test_logcat_scan_lines_and_findings():
    lines = [
        "01-01 00:00:00.000 D/Auth: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.sig",
        "01-01 00:00:00.001 I/App: started",
    ]
    findings = logcat.hits_to_findings(logcat.scan_lines(lines))
    assert any(f["cwe"] == "CWE-532" for f in findings)


def test_logcat_scan_file(tmp_path):
    p = tmp_path / "log.txt"
    p.write_text("D/Net: password = s3cr3tvalue\n")
    findings = logcat.scan_file(str(p))
    assert any("Password" in f["title"] for f in findings)


# -- device parsing (fake adb) ---------------------------------------------
def test_list_devices_parses_model(monkeypatch):
    adb = AdbClient(adb_path="/usr/bin/true")
    out = ("List of devices attached\n"
           "emulator-5554 device product:sdk model:Pixel_6 device:generic\n"
           "ZY223 offline\n")
    monkeypatch.setattr(adb, "run",
                        lambda *a, **k: subprocess.CompletedProcess([], 0, out, ""))
    devices = adb.list_devices()
    assert devices[0].serial == "emulator-5554" and devices[0].model == "Pixel_6"
    assert devices[1].state == "offline"


def test_ensure_device_requires_serial_when_ambiguous(monkeypatch):
    adb = AdbClient(adb_path="/usr/bin/true")
    out = ("List of devices attached\n"
           "dev1 device\n"
           "dev2 device\n")
    monkeypatch.setattr(adb, "run",
                        lambda *a, **k: subprocess.CompletedProcess([], 0, out, ""))
    with pytest.raises(DeviceError):
        adb.ensure_device()


def test_missing_adb_raises():
    adb = AdbClient(adb_path=None)
    assert not adb.available
    with pytest.raises(DeviceError):
        adb.list_devices()


# -- observation -> findings ----------------------------------------------
def test_observations_to_findings():
    analyzer = DynamicAnalyzer(DynamicConfig(package="com.example"))
    obs = [
        {"channel": "insecure-random", "data": {}},
        {"channel": "webview", "data": {"setting": "javaScriptEnabled", "value": True}},
        {"channel": "raw-sql", "data": {"op": "execSQL"}},
        {"channel": "log", "data": {"level": "d", "tag": "Auth",
                                     "message": "api_key = AIzaSyA1234567890123456789012345678901234"}},
    ]
    findings = analyzer._findings_from_observations(obs)
    titles = " ".join(f["title"] for f in findings)
    assert "java.util.Random" in titles
    assert "WebView JavaScript" in titles
    assert "Raw SQL" in titles
    assert "Sensitive data logged" in titles


def test_normalize_frida_findings_dedupes():
    analyzer = DynamicAnalyzer(DynamicConfig(package="com.example"))
    raw = [
        {"check": "weak_cipher", "severity": "HIGH", "title": "x", "detail": "DES",
         "owasp": "M5"},
        {"check": "weak_cipher", "severity": "HIGH", "title": "x", "detail": "DES",
         "owasp": "M5"},
    ]
    assert len(analyzer._normalize_frida_findings(raw)) == 1


# -- orchestrator degrades without a device --------------------------------
def test_analyze_degrades_without_device(tmp_path):
    flows = tmp_path / "flows.jsonl"
    flows.write_text(json.dumps({"scheme": "http", "host": "api.example.com",
                                 "path": "/x"}) + "\n")
    cfg = DynamicConfig(
        package="com.example", adb_path="/nonexistent/adb",
        enable_frida=False, enable_logcat=False, enable_traffic=False,
        flows_path=str(flows),
    )
    results = DynamicAnalyzer(cfg).analyze()
    assert results["analysis_type"] == "dynamic"
    assert "device" in results["metadata"]["component_errors"]
    assert "flows" in results["metadata"]["components_run"]
    assert any(f["title"].startswith("Cleartext HTTP") for f in results["findings"])
