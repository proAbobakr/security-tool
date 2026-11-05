# Android Security Analyzer

A comprehensive OWASP-based security analysis tool for Android applications. Performs **static analysis** of APK, AAR, and JAR files and **dynamic (runtime) analysis** of a live app on a device/emulator, following the OWASP Mobile Top 10, MASVS, and the Mobile Application Security Testing Guide (MASTG).

> Intended for authorized assessment of applications you own or are explicitly permitted to test.

## Features

### 🔍 Comprehensive Security Analysis
- **OWASP Mobile Top 10** coverage
- **MASVS** compliance checking
- **Static** analysis of **APK**, **AAR**, and **JAR** files
- **Dynamic** (runtime) analysis of a live app: crypto & TLS trust handling, insecure storage, WebView config, and sensitive data in logs/network traffic
- Detailed security findings with remediation recommendations

### 📊 Multiple Report Formats
- HTML (interactive, styled reports)
- JSON (machine-readable)
- Markdown (documentation-friendly)
- Console (quick feedback)

### 🔧 Easy Integration
- Gradle plugin for Android projects
- Command-line interface
- CI/CD pipeline support
- Batch analysis capabilities

## Security Checks

Based on **OWASP Mobile Top 10**:

| Category | Checks Performed |
|----------|-----------------|
| **M1: Improper Platform Usage** | Dangerous permissions, exported components, WebView security, deep link validation |
| **M2: Insecure Data Storage** | Backup configuration, external storage usage, insecure storage patterns |
| **M3: Insecure Communication** | Network security config, cleartext traffic, certificate pinning |
| **M4: Insecure Authentication** | Authentication mechanism analysis |
| **M5: Insufficient Cryptography** | Weak algorithms, hardcoded secrets, insecure random number generation |
| **M6: Insecure Authorization** | Authorization checks, permission enforcement |
| **M7: Client Code Quality** | Debuggable flag, logging practices, SQL injection risks |
| **M8: Code Tampering** | Root detection, integrity checks |
| **M9: Reverse Engineering** | Code obfuscation, native library protection |
| **M10: Extraneous Functionality** | Test code, backdoor detection |

## Installation

### Prerequisites
- Python 3.8+
- Java 11+ (for Gradle plugin)

### Install Python Package
```bash
# Clone the repository
git clone https://github.com/yourusername/security-tool.git
cd security-tool

# Install dependencies
pip install -r requirements.txt

# Install the package
pip install -e .
```

### Build Gradle Plugin
```bash
cd gradle-plugin
./gradlew build
```

## Usage

### Command-Line Interface

#### Basic Analysis
```bash
# Analyze an APK file (HTML report)
python -m analyzer.cli analyze app.apk

# Specify output format and file
python -m analyzer.cli analyze app.apk --format json --output report.json

# Filter by severity level
python -m analyzer.cli analyze app.apk --severity HIGH

# Console output for quick feedback
python -m analyzer.cli analyze app.apk --format console
```

#### Batch Analysis
```bash
# Analyze all APKs in a directory
python -m analyzer.cli batch /path/to/apks --pattern "*.apk" --output-dir reports

# Analyze AAR files
python -m analyzer.cli batch /path/to/libraries --pattern "*.aar" --format json
```

#### Get Information
```bash
# Display analyzer capabilities
python -m analyzer.cli info
```

### Dynamic Analysis (runtime)

Observes how the app actually behaves at runtime and reports insecure patterns. See [docs/DYNAMIC_ANALYSIS.md](docs/DYNAMIC_ANALYSIS.md) for the full setup.

```bash
# Install optional runtime dependencies (Frida + mitmproxy)
pip install -r requirements-dynamic.txt      # or: pip install .[dynamic]

# Pre-compile the Frida observer agent (bundles frida-java-bridge; optional)
python -m analyzer.cli build-agent

# Analyze a running package on the connected device/emulator
python -m analyzer.cli dynamic com.example.app --duration 30 -f html -o dyn.html

# Install an APK, instrument it, and also capture live traffic via mitmdump
python -m analyzer.cli dynamic com.example.app --apk app.apk --traffic --proxy-port 8080

# No device? Analyze an existing HAR or flow capture offline
python -m analyzer.cli dynamic com.example.app --no-frida --no-logcat --har session.har -f console
```

**Requirements:** `adb` (Android platform-tools) and a `frida-server` on the device matching your Frida version; `mitmproxy` only for `--traffic`. Any unavailable component is skipped with a clear message rather than failing the run — so a partial setup (e.g. offline HAR analysis) still produces a report.

### Gradle Integration

#### 1. Add Plugin to Project

In your root `build.gradle`:
```groovy
buildscript {
    dependencies {
        classpath files('path/to/android-security-analyzer-plugin-1.0.0.jar')
    }
}
```

In your app `build.gradle`:
```groovy
apply plugin: 'com.security.analyzer.android'

securityAnalyzer {
    minSeverity = 'MEDIUM'
    reportFormat = 'html'
    reportOutputDir = 'build/reports/security'
    failOnError = false
}
```

#### 2. Run Analysis
```bash
# Analyze the APK after building
./gradlew assembleRelease analyzeSecurityFindings

# Run as part of checks
./gradlew check
```

See [examples/sample-gradle-integration.gradle](examples/sample-gradle-integration.gradle) for complete configuration.

### Configuration

Create a `config.yaml` file to customize analysis:

```yaml
min_severity: MEDIUM
report_format: html
fail_on_critical: true

checks:
  check_permissions: true
  check_debuggable: true
  check_backup_allowed: true
  check_network_security: true
  # ... more checks

dangerous_permissions:
  - android.permission.READ_SMS
  - android.permission.CAMERA
  # ... more permissions
```

## Report Examples

### HTML Report
Interactive report with:
- Executive summary with severity counts
- Application metadata
- Detailed findings with OWASP categories
- CWE references
- Remediation recommendations

### JSON Report
```json
{
  "generated_at": "2025-01-15T10:30:00",
  "results": {
    "file_path": "app.apk",
    "file_type": "APK",
    "findings": [
      {
        "severity": "HIGH",
        "title": "Application is Debuggable",
        "description": "The application has android:debuggable=\"true\"",
        "owasp_category": "M7: Client Code Quality",
        "cwe": "CWE-489",
        "recommendation": "Set android:debuggable=\"false\" in production"
      }
    ]
  }
}
```

## CI/CD Integration

### GitHub Actions
```yaml
name: Security Analysis

on: [push, pull_request]

jobs:
  security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2

      - name: Set up Python
        uses: actions/setup-python@v2
        with:
          python-version: '3.9'

      - name: Install dependencies
        run: |
          pip install -r requirements.txt
          pip install -e .

      - name: Build APK
        run: ./gradlew assembleRelease

      - name: Run Security Analysis
        run: |
          python -m analyzer.cli analyze app/build/outputs/apk/release/app-release.apk \
            --format html --output security-report.html --severity MEDIUM

      - name: Upload Report
        uses: actions/upload-artifact@v2
        with:
          name: security-report
          path: security-report.html
```

### GitLab CI
```yaml
security_analysis:
  stage: test
  script:
    - pip install -r requirements.txt
    - python -m analyzer.cli analyze app.apk --format json --output report.json
  artifacts:
    reports:
      junit: report.json
    paths:
      - report.json
```

## Development

### Project Structure
```
security-tool/
├── analyzer/                 # Core analyzer package
│   ├── __init__.py
│   ├── core.py               # Static analyzer logic
│   ├── owasp_checks.py       # Static OWASP security checks
│   ├── reporter.py           # Report generation (HTML/JSON/MD/console)
│   ├── cli.py                # Command-line interface
│   └── dynamic/              # Dynamic (runtime) analysis
│       ├── runner.py         # Orchestrator -> unified findings
│       ├── device.py         # adb wrapper
│       ├── instrumentation.py# Frida session + agent bundling
│       ├── traffic.py        # mitmproxy capture + flow/HAR analysis
│       ├── logcat.py         # logcat sensitive-data scanner
│       ├── patterns.py       # shared sensitive-data patterns
│       ├── agents/observer.js# Frida observer agent (source)
│       └── addons/flow_logger.py  # mitmdump addon
├── gradle-plugin/            # Gradle plugin
├── examples/                 # Usage examples
├── tests/                    # Unit tests
├── docs/                     # Documentation
├── config.yaml               # Configuration file
├── requirements.txt          # Core dependencies
├── requirements-dynamic.txt  # Optional runtime-analysis dependencies
├── setup.py                  # Package setup
└── README.md
```

### Running Tests
```bash
# Run analyzer tests
python -m pytest tests/

# Test Gradle plugin
cd gradle-plugin
./gradlew test
```

## OWASP Mobile Security Resources

- [OWASP Mobile Top 10](https://owasp.org/www-project-mobile-top-10/)
- [OWASP MASVS](https://github.com/OWASP/owasp-masvs)
- [OWASP Mobile Security Testing Guide](https://owasp.org/www-project-mobile-security-testing-guide/)

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Add tests for new features
4. Submit a pull request

## Security Considerations

This tool is designed for:
- ✅ Security testing of your own applications
- ✅ Authorized penetration testing
- ✅ Security research and education
- ✅ Code review and auditing

**Not for:**
- ❌ Analyzing applications without permission
- ❌ Malicious purposes
- ❌ Bypassing security controls

## License

MIT License - see LICENSE file for details

## Disclaimer

This tool provides automated security analysis based on OWASP guidelines. It should be used as part of a comprehensive security assessment, not as the sole security measure. Always perform manual security reviews and penetration testing for production applications.

## Support

- 📖 [Documentation](docs/)
- 🐛 [Issue Tracker](https://github.com/yourusername/security-tool/issues)
- 💬 [Discussions](https://github.com/yourusername/security-tool/discussions)

## Changelog

### Version 1.1.0
- Dynamic (runtime) analysis: `dynamic` and `build-agent` commands
- Frida observer agent for crypto, TLS trust, storage, and WebView observation
- Network traffic analysis via mitmproxy, plus offline HAR/JSONL analysis
- logcat sensitive-data scanning
- Graceful degradation when adb/frida/mitmproxy are unavailable
- Unit test suite for the dynamic pipeline

### Version 1.0.0
- Initial release
- OWASP Mobile Top 10 coverage
- Support for APK, AAR, and JAR files
- Multiple report formats
- Gradle plugin integration
- CLI interface
