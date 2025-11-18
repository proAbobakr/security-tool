#!/bin/bash

# Android Security Analyzer - CLI Usage Examples

# Install dependencies
pip install -r requirements.txt

# Basic analysis of an APK file
python -m analyzer.cli analyze app.apk

# Analyze with JSON output
python -m analyzer.cli analyze app.apk --format json --output report.json

# Analyze with specific severity filter
python -m analyzer.cli analyze app.apk --severity HIGH --format html

# Analyze AAR file
python -m analyzer.cli analyze library.aar --format markdown --output library-report.md

# Analyze JAR file
python -m analyzer.cli analyze classes.jar

# Batch analyze multiple APKs
python -m analyzer.cli batch /path/to/apk/directory --pattern "*.apk" --output-dir reports

# Display analyzer information
python -m analyzer.cli info

# Examples with real scenarios:

# 1. CI/CD Integration - Fail build on critical findings
python -m analyzer.cli analyze release.apk --severity CRITICAL --format json
if [ $? -eq 2 ]; then
    echo "Critical security issues found! Build failed."
    exit 1
fi

# 2. Generate HTML report for manual review
python -m analyzer.cli analyze debug.apk --format html --output security-report.html
echo "Report generated: security-report.html"

# 3. Quick console output for debugging
python -m analyzer.cli analyze app.apk --format console --severity MEDIUM

# 4. Comprehensive analysis with all findings
python -m analyzer.cli analyze app.apk --severity INFO --format markdown --output full-report.md
