# APK Security Analyzer

A comprehensive desktop security analysis tool for Android APK and AAR files, built with Kotlin and Jetpack Compose Material 3.

## 🚀 Features

### 1. **Comprehensive Malware Detection**
- Advanced heuristic analysis
- Signature-based detection
- Behavioral pattern recognition
- Detects: Trojans, Spyware, Adware, Ransomware, Rootkits, Banking malware, SMS fraud
- Suspicious API call detection
- Network activity monitoring
- Data exfiltration detection
- Dynamic code loading analysis
- Root access detection

### 2. **OWASP Mobile Top 10 Compliance Checker**
Complete security assessment based on OWASP Mobile Top 10:
- **M1**: Improper Platform Usage
- **M2**: Insecure Data Storage
- **M3**: Insecure Communication
- **M4**: Insecure Authentication
- **M5**: Insufficient Cryptography
- **M6**: Insecure Authorization
- **M7**: Client Code Quality
- **M8**: Code Tampering
- **M9**: Reverse Engineering
- **M10**: Extraneous Functionality

### 3. **DEX Bytecode Analysis**
- Deep DEX file analysis using dexlib2
- Method call tracing
- String usage analysis
- Class hierarchy examination
- Obfuscation detection

### 4. **Runtime Instrumentation (Frida-like)**
Modify APK behavior at runtime:
- Add hooks to any method (BEFORE, AFTER, REPLACE)
- Generate hook templates automatically
- Quick presets for common tasks:
  - SSL pinning bypass
  - Network call tracing
  - Cryptographic operation monitoring
- Export/import hook scripts
- Monitor method parameters and return values

### 5. **APK/AAR Analysis**
- Package information extraction
- Permission analysis with risk assessment
- Certificate and signature verification
- Component enumeration (Activities, Services, Receivers, Providers)
- Manifest parsing
- Dangerous permission highlighting

### 6. **Security Scoring**
- Overall security score (0-100)
- Risk level assessment (Critical, High, Medium, Low, Info)
- Confidence-based malware detection
- Category-specific OWASP scores

### 7. **Modern Material 3 UI**
- Beautiful, intuitive interface
- Dark theme support
- Tabbed navigation:
  - Overview
  - Malware Detection
  - OWASP Checks
  - Code Analysis
  - Runtime Instrumentation
- Real-time analysis feedback
- Interactive finding cards

## 🛠️ Technology Stack

- **Language**: Kotlin 1.9.21
- **UI Framework**: Jetpack Compose Desktop 1.5.11
- **Design**: Material 3
- **APK Parsing**: apk-parser 2.6.10
- **DEX Analysis**: smali/dexlib2 2.5.2
- **Bytecode**: ASM 9.6
- **Build Tool**: Gradle 8.5

## 📋 Requirements

- JDK 17 or higher
- Gradle 8.x
- 2GB RAM minimum
- Operating Systems: Windows, macOS, Linux

## 🏗️ Building the Project

### Using Gradle Wrapper (Recommended)

```bash
# Clone the repository
git clone <repository-url>
cd security-tool

# Build the project
./gradlew build

# Run the application
./gradlew run

# Create distributable packages
./gradlew packageDmg     # macOS
./gradlew packageMsi     # Windows
./gradlew packageDeb     # Linux
```

### Manual Build

```bash
gradle build
gradle run
```

## 🚦 Usage

### 1. Launch the Application
```bash
./gradlew run
```

### 2. Select an APK/AAR File
- Click "Select APK/AAR File" button
- Choose your target file
- Wait for analysis to complete

### 3. Review Results

#### Overview Tab
- View overall security score
- Check risk level
- See detected malware types
- Quick summary of findings

#### Malware Detection Tab
- Detailed malware analysis
- Suspicious API calls
- Network connections
- Security findings with recommendations

#### OWASP Checks Tab
- OWASP Mobile Top 10 compliance
- Category-specific scores
- Detailed findings per category
- Remediation recommendations

#### Code Analysis Tab
- Permission analysis
- Component enumeration
- Certificate information
- Dangerous permission highlighting

#### Instrumentation Tab
- Add custom hooks to methods
- Use quick presets:
  - Bypass SSL pinning
  - Trace network calls
  - Monitor crypto operations
- Generate hook code templates
- Export/import hook scripts

## 🔍 Detection Capabilities

### Malware Detection Techniques

1. **Permission Analysis**
   - Dangerous permission combinations
   - SMS fraud indicators
   - Spyware patterns

2. **API Monitoring**
   - Runtime execution (exec, ProcessBuilder)
   - Dynamic code loading (DexClassLoader)
   - Device ID access
   - SMS operations
   - Network connections

3. **Behavioral Analysis**
   - Root access attempts
   - Data exfiltration patterns
   - Obfuscation detection
   - Dynamic code loading
   - Crypto mining indicators
   - Ransomware behavior

### OWASP Checks

Each check analyzes specific security aspects:
- Hardcoded credentials
- Weak cryptography (DES, MD5, SHA-1)
- Insecure storage (SharedPreferences, SQLite)
- HTTP vs HTTPS usage
- SSL certificate validation
- SQL injection vulnerabilities
- Debug code in production
- Code obfuscation level

## 📊 Output Reports

The tool provides:
- Interactive UI with real-time results
- Security score and risk assessment
- Categorized findings
- Detailed recommendations
- Export capabilities (HTML/JSON)

## 🎯 Use Cases

1. **Security Auditing**: Analyze APKs for security vulnerabilities
2. **Malware Research**: Study malicious applications
3. **Compliance Testing**: Verify OWASP Mobile Top 10 compliance
4. **Penetration Testing**: Runtime code modification for testing
5. **Code Review**: Analyze third-party libraries (AAR)
6. **Education**: Learn about Android security best practices

## 🔐 Security Features

- Detects 10+ types of malware
- Checks against OWASP Mobile Top 10
- Analyzes permissions and certificates
- Identifies insecure code patterns
- Detects hardcoded credentials
- Finds weak cryptography
- Identifies data leakage
- Detects obfuscation and anti-analysis

## 🎨 UI Features

- Material 3 design language
- Dark theme optimized
- Intuitive tab-based navigation
- Color-coded security levels
- Interactive expandable cards
- Real-time progress indicators
- File drag-and-drop support

## 📁 Project Structure

```
security-tool/
├── src/main/kotlin/com/security/apkanalyzer/
│   ├── Main.kt                          # Application entry point
│   ├── domain/
│   │   └── Models.kt                    # Data models
│   ├── analyzer/
│   │   ├── ApkParser.kt                 # APK/AAR parsing
│   │   ├── DexAnalyzer.kt               # DEX bytecode analysis
│   │   └── SecurityAnalyzer.kt          # Main analysis coordinator
│   ├── detector/
│   │   ├── MalwareDetector.kt           # Malware detection engine
│   │   └── OwaspChecker.kt              # OWASP compliance checker
│   ├── instrumentation/
│   │   └── RuntimeInstrumentor.kt       # Runtime code modification
│   ├── ui/
│   │   ├── MainScreen.kt                # Main UI
│   │   ├── theme/Theme.kt               # Material 3 theme
│   │   └── components/                  # UI components
│   │       ├── OverviewTab.kt
│   │       ├── MalwareDetectionTab.kt
│   │       ├── OwaspChecksTab.kt
│   │       ├── CodeAnalysisTab.kt
│   │       └── InstrumentationTab.kt
│   └── utils/
│       └── ReportExporter.kt            # Report generation
├── build.gradle.kts                     # Build configuration
├── settings.gradle.kts                  # Project settings
└── README.md                            # This file
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## ⚖️ License

This tool is for educational and authorized security testing purposes only.

## 🙏 Acknowledgments

- OWASP Mobile Security Project
- Frida dynamic instrumentation toolkit (inspiration)
- Android security community
- dexlib2 and smali projects

## 📚 References

- [OWASP Mobile Top 10](https://owasp.org/www-project-mobile-top-10/)
- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)
- [Frida - Dynamic Instrumentation Toolkit](https://frida.re/)

## 🔄 Version History

### Version 1.0.0 (Current)
- Initial release
- Comprehensive malware detection
- OWASP Mobile Top 10 compliance checking
- DEX bytecode analysis
- Runtime instrumentation (Frida-like)
- Material 3 UI
- APK/AAR support

---

**Note**: This tool is designed for security professionals, researchers, and developers to analyze Android applications for security issues. Always obtain proper authorization before analyzing applications you don't own.
