# Installation Guide

## Prerequisites

### System Requirements
- **Python**: 3.8 or higher
- **Java**: 11 or higher (for Gradle plugin)
- **Operating System**: Linux, macOS, or Windows
- **Memory**: Minimum 2GB RAM
- **Disk Space**: 500MB for dependencies

### Required Tools
- pip (Python package manager)
- Git
- Gradle 7.0+ (for Gradle plugin)

## Installation Methods

### Method 1: Install from Source (Recommended)

#### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/security-tool.git
cd security-tool
```

#### 2. Create Virtual Environment (Optional but Recommended)
```bash
# Create virtual environment
python3 -m venv venv

# Activate virtual environment
# On Linux/macOS:
source venv/bin/activate

# On Windows:
venv\Scripts\activate
```

#### 3. Install Python Dependencies
```bash
pip install -r requirements.txt
```

#### 4. Install the Package
```bash
# Development mode (recommended for development)
pip install -e .

# Or install normally
pip install .
```

#### 5. Verify Installation
```bash
python -m analyzer.cli info
```

### Method 2: Install via pip (Coming Soon)
```bash
pip install android-security-analyzer
```

## Gradle Plugin Installation

### Build the Plugin

#### 1. Navigate to Plugin Directory
```bash
cd gradle-plugin
```

#### 2. Build the Plugin
```bash
./gradlew build
```

The plugin JAR will be created at:
```
gradle-plugin/build/libs/android-security-analyzer-plugin-1.0.0.jar
```

### Install Plugin in Your Project

#### Option A: Local JAR File
In your project's root `build.gradle`:
```groovy
buildscript {
    dependencies {
        classpath files('/path/to/android-security-analyzer-plugin-1.0.0.jar')
    }
}
```

#### Option B: Maven Local Repository
```bash
# Publish to Maven Local
cd gradle-plugin
./gradlew publishToMavenLocal
```

Then in your `build.gradle`:
```groovy
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath 'com.security.analyzer:android-security-analyzer-plugin:1.0.0'
    }
}
```

## Dependency Installation Details

### Core Python Dependencies

#### androguard
- **Purpose**: APK decompilation and analysis
- **Installation**: `pip install androguard`
- **Issues**: May require build tools on some systems

#### lxml
- **Purpose**: XML parsing for AndroidManifest.xml
- **Installation**: `pip install lxml`
- **Platform-specific**:
  - Linux: May require `libxml2-dev` and `libxslt-dev`
  - macOS: Usually works out of the box
  - Windows: May need Visual C++ build tools

#### Other Dependencies
```bash
pip install requests pyyaml jinja2 colorama click python-magic
```

### Platform-Specific Setup

#### Ubuntu/Debian
```bash
# Install system dependencies
sudo apt-get update
sudo apt-get install python3-dev libxml2-dev libxslt-dev

# Install Python packages
pip install -r requirements.txt
```

#### macOS
```bash
# Install Homebrew (if not installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install dependencies
brew install python@3.9
pip3 install -r requirements.txt
```

#### Windows
```powershell
# Install Python from python.org
# Open PowerShell as Administrator

# Install dependencies
pip install -r requirements.txt
```

## Troubleshooting

### Common Issues

#### Issue: `androguard` installation fails
**Solution**:
```bash
# Install build dependencies
# Ubuntu/Debian:
sudo apt-get install python3-dev build-essential

# Then retry:
pip install androguard
```

#### Issue: `lxml` installation fails
**Solution**:
```bash
# Ubuntu/Debian:
sudo apt-get install libxml2-dev libxslt-dev

# macOS:
brew install libxml2

# Then retry:
pip install lxml
```

#### Issue: Permission denied
**Solution**:
```bash
# Use virtual environment or:
pip install --user -r requirements.txt
```

#### Issue: Command not found after installation
**Solution**:
```bash
# Ensure PATH includes pip installation directory
export PATH="$HOME/.local/bin:$PATH"

# Or use:
python -m analyzer.cli
```

### Verify Installation

#### Check Python Package
```bash
python -c "import analyzer; print(analyzer.__version__)"
```

#### Check CLI
```bash
python -m analyzer.cli --version
```

#### Check Gradle Plugin
```bash
cd gradle-plugin
./gradlew tasks | grep security
```

## Uninstallation

### Remove Python Package
```bash
pip uninstall android-security-analyzer
```

### Remove Gradle Plugin
Remove the plugin dependency from your `build.gradle` files.

### Clean Virtual Environment
```bash
deactivate
rm -rf venv/
```

## Next Steps

After installation:
1. Review the [Usage Guide](USAGE.md)
2. Check [Examples](../examples/)
3. Read [Configuration Guide](CONFIGURATION.md)
4. Try analyzing a sample APK

## Support

If you encounter issues:
1. Check [Troubleshooting](#troubleshooting) section
2. Search [GitHub Issues](https://github.com/yourusername/security-tool/issues)
3. Create a new issue with:
   - Python version: `python --version`
   - OS and version
   - Error messages
   - Installation method used
