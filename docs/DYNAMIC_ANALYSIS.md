# Dynamic (Runtime) Analysis Guide

Dynamic analysis complements the static scanner by observing how an app
actually behaves while running on a device or emulator. It catches issues that
are invisible to static inspection — weak crypto chosen at runtime, custom TLS
trust handling, secrets written to storage or logs, and sensitive data crossing
the network.

> **Authorized use only.** Run this against applications you own or have
> explicit permission to test. The instrumentation *observes and reports*
> behavior; its hooks always delegate to the app's original methods and do not
> bypass, disable, or weaken any security control.

## What it checks

| Component | OWASP | Examples |
|-----------|-------|----------|
| Frida agent — crypto | M5 | `Cipher.getInstance("DES")`, ECB mode, weak `MessageDigest` (MD5/SHA-1), `java.util.Random`, `SecretKeySpec` key sizes |
| Frida agent — TLS | M3 | Custom `TrustManager` / `HostnameVerifier` installed (flagged for review) |
| Frida agent — storage | M2 | Sensitive keys in `SharedPreferences`, world-accessible `openFileOutput`, raw SQL |
| Frida agent — WebView | M1/M7 | `setJavaScriptEnabled`, `setAllowFileAccess`, `addJavascriptInterface` |
| logcat scanner | M7 | Tokens, passwords, keys, PII emitted to the system log (CWE-532) |
| Traffic analysis | M3 | Cleartext HTTP, secrets in URLs/bodies/headers, missing HSTS |

All matched secrets are **redacted** in the report (only a short prefix is kept)
so the report itself never becomes a new leak.

## Prerequisites

1. **adb** — Android platform-tools on `PATH`.
2. **frida-server** running on the device, matching your installed Frida
   version (`pip show frida`). Launch it and confirm `frida-ps -U` works.
3. **mitmproxy** — only needed for live `--traffic` capture. The device must
   route through the proxy and trust its CA for TLS interception (standard
   MASTG setup).

Install the optional Python dependencies:

```bash
pip install -r requirements-dynamic.txt      # or: pip install .[dynamic]
```

## The Frida agent and the Java bridge

Frida 17 stopped bundling the Java bridge, so the observer agent is compiled
with `frida-java-bridge` resolved locally. The analyzer does this automatically
on first use (via Frida's package manager, falling back to `npm install`), but
you can pre-build it — useful in CI with no network at test time:

```bash
python -m analyzer.cli build-agent        # writes analyzer/dynamic/agents/observer.bundle.js
python -m analyzer.cli build-agent --rebuild
```

The compiled bundle and `node_modules` are git-ignored build artifacts.

## Usage

```bash
# Spawn and instrument a package for 30s, HTML report
python -m analyzer.cli dynamic com.example.app --duration 30 -f html -o dyn.html

# Attach to an already-running process instead of spawning
python -m analyzer.cli dynamic com.example.app --attach

# Install an APK first, then instrument and capture traffic
python -m analyzer.cli dynamic com.example.app --apk app.apk --traffic --proxy-port 8080

# Target a specific device when several are attached
python -m analyzer.cli dynamic com.example.app --serial emulator-5554

# Offline: analyze an existing capture with no device
python -m analyzer.cli dynamic com.example.app --no-frida --no-logcat --har session.har
python -m analyzer.cli dynamic com.example.app --no-frida --no-logcat --flows flows.jsonl
```

### Exit codes

Same convention as static analysis: `2` if any CRITICAL finding, `1` if any
HIGH finding, otherwise `0` — convenient as a CI gate.

## Graceful degradation

Each component is independent. If adb finds no device, if frida-server isn't
running, or if mitmproxy isn't installed, that component is **skipped with a
recorded message** and the rest of the run continues. The report's
`metadata.component_errors` lists what was skipped and why, and
`metadata.components_run` lists what actually executed.

## Capturing traffic manually

If you prefer to capture outside the tool, run mitmproxy with the bundled addon
and analyze the result offline:

```bash
mitmdump -s analyzer/dynamic/addons/flow_logger.py --set flow_out=flows.jsonl --listen-port 8080
# ... drive the app ...
python -m analyzer.cli dynamic com.example.app --no-frida --no-logcat --flows flows.jsonl
```

Any tool that exports standard HAR (browser devtools, Charles, etc.) works via
`--har`.

## Limitations

- Findings are **observational**: a custom `TrustManager`, for instance, is
  flagged for manual review rather than asserted insecure, because safe and
  unsafe implementations share the same API surface.
- Runtime coverage is only as good as the exercised code paths. Drive the
  relevant flows (login, payments, etc.) during the instrumentation window, or
  extend `--duration`.
- Hooks are wrapped in guards; an API absent on a given Android version is
  skipped and noted in `component_errors.frida_hooks` rather than aborting.
