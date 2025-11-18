# OWASP Security Checks Reference

This document details all security checks performed by the Android Security Analyzer, based on the OWASP Mobile Top 10 and MASVS (Mobile Application Security Verification Standard).

## OWASP Mobile Top 10 Coverage

### M1: Improper Platform Usage

Misuse of platform features or failure to use platform security controls properly.

#### Checks Performed:

**1. Dangerous Permissions**
- **What**: Identifies use of dangerous/sensitive permissions
- **Risk**: Excessive permissions increase attack surface
- **Detected Permissions**:
  - `READ_SMS`, `SEND_SMS` - SMS access
  - `READ_CONTACTS`, `WRITE_CONTACTS` - Contact access
  - `ACCESS_FINE_LOCATION` - Precise location
  - `CAMERA` - Camera access
  - `RECORD_AUDIO` - Microphone access
  - `READ_PHONE_STATE` - Device identifiers
  - And more...
- **Severity**: MEDIUM
- **Recommendation**: Only request necessary permissions, implement runtime permission requests

**2. Exported Components**
- **What**: Activities, Services, Receivers without proper protection
- **Risk**: Other apps can invoke these components
- **Detection**: Checks for `android:exported="true"` without permissions
- **Severity**: MEDIUM (Activities/Receivers), HIGH (Services)
- **Recommendation**: Set `android:exported="false"` or add permission requirements

**3. WebView Security**
- **What**: Insecure WebView configurations
- **Risk**: JavaScript injection, file access vulnerabilities
- **Checks**:
  - `setJavaScriptEnabled(true)`
  - `setAllowFileAccess(true)`
  - `setAllowUniversalAccessFromFileURLs(true)`
- **Severity**: HIGH
- **Recommendation**: Disable unnecessary WebView features, validate all URLs

**4. Deep Link Validation**
- **What**: Improper deep link handling
- **Risk**: Intent redirection, data injection
- **Severity**: INFO
- **Recommendation**: Validate and sanitize all deep link inputs

---

### M2: Insecure Data Storage

Insecure storage of sensitive data on the device.

#### Checks Performed:

**1. Backup Configuration**
- **What**: `android:allowBackup="true"`
- **Risk**: Sensitive data exposed through backup mechanisms
- **Detection**: Checks AndroidManifest.xml
- **Severity**: MEDIUM
- **CWE**: CWE-200 (Exposure of Sensitive Information)
- **Recommendation**:
  ```xml
  android:allowBackup="false"
  <!-- OR -->
  android:fullBackupContent="@xml/backup_rules"
  ```

**2. External Storage Access**
- **What**: `WRITE_EXTERNAL_STORAGE` permission
- **Risk**: External storage is world-readable
- **Severity**: MEDIUM
- **CWE**: CWE-312 (Cleartext Storage of Sensitive Information)
- **Recommendation**: Use internal storage or encrypt data

**3. Content Providers**
- **What**: Exposed content providers
- **Risk**: Unauthorized data access
- **Severity**: HIGH
- **CWE**: CWE-926 (Improper Export of Android Application Components)
- **Recommendation**: Implement proper read/write permissions

---

### M3: Insecure Communication

Lack of proper encryption for data in transit.

#### Checks Performed:

**1. Cleartext Traffic**
- **What**: `android:usesCleartextTraffic="true"`
- **Risk**: Data transmitted over HTTP can be intercepted
- **Severity**: HIGH
- **CWE**: CWE-319 (Cleartext Transmission of Sensitive Information)
- **Recommendation**:
  ```xml
  android:usesCleartextTraffic="false"
  ```

**2. Network Security Configuration**
- **What**: Missing `networkSecurityConfig`
- **Risk**: No enforcement of HTTPS, no certificate pinning
- **Severity**: LOW
- **Recommendation**: Implement network_security_config.xml:
  ```xml
  <network-security-config>
      <base-config cleartextTrafficPermitted="false"/>
      <domain-config>
          <domain includeSubdomains="true">example.com</domain>
          <pin-set>
              <pin digest="SHA-256">base64encodedpin==</pin>
          </pin-set>
      </domain-config>
  </network-security-config>
  ```

**3. Certificate Pinning**
- **What**: Absence of certificate pinning
- **Risk**: Man-in-the-middle attacks
- **Severity**: MEDIUM
- **Recommendation**: Implement certificate pinning for sensitive communications

---

### M4: Insecure Authentication

Weak or missing authentication mechanisms.

#### Checks Performed:

**1. Authentication Mechanisms**
- **What**: Analysis of authentication implementation
- **Risk**: Weak authentication allows unauthorized access
- **Detection**: Code pattern analysis (planned)
- **Recommendation**: Use Android Keystore, BiometricPrompt

---

### M5: Insufficient Cryptography

Weak encryption algorithms or improper use of cryptography.

#### Checks Performed:

**1. Weak Cryptographic Algorithms**
- **What**: Usage of deprecated/weak algorithms
- **Detected Patterns**:
  - DES encryption
  - MD5 hashing
  - SHA-1 hashing
  - ECB mode
  - `Random()` instead of `SecureRandom`
- **Severity**: HIGH
- **Recommendation**: Use approved algorithms:
  - Encryption: AES-256 with GCM or CBC mode
  - Hashing: SHA-256, SHA-384, SHA-512
  - Random: SecureRandom

**2. Hardcoded Secrets**
- **What**: Hardcoded API keys, passwords, tokens
- **Detection Patterns**:
  - `api_key`, `api_secret`
  - `password = "..."`
  - `secret = "..."`
  - `token = "..."`
  - AWS credentials
  - Private keys
- **Severity**: HIGH
- **CWE**: CWE-798 (Use of Hard-coded Credentials)
- **Recommendation**: Use Android Keystore, encrypted preferences, or remote configuration

**3. Insecure Random Number Generation**
- **What**: Use of `java.util.Random` for security purposes
- **Risk**: Predictable random numbers
- **Severity**: HIGH
- **Recommendation**: Always use `SecureRandom` for cryptographic operations

---

### M6: Insecure Authorization

Improper authorization checks.

#### Checks Performed:

**1. Authorization Enforcement**
- **What**: Missing authorization checks on exported components
- **Risk**: Unauthorized access to protected functionality
- **Severity**: HIGH
- **CWE**: CWE-927 (Use of Implicit Intent for Sensitive Communication)
- **Recommendation**: Implement permission checks, use explicit intents

---

### M7: Client Code Quality

Code-level vulnerabilities.

#### Checks Performed:

**1. Debuggable Application**
- **What**: `android:debuggable="true"` in production
- **Risk**: Allows debugging and runtime manipulation
- **Severity**: HIGH
- **CWE**: CWE-489 (Active Debug Code)
- **Recommendation**:
  ```xml
  <!-- In production builds -->
  android:debuggable="false"
  ```

**2. Logging Practices**
- **What**: Use of verbose logging
- **Risk**: Sensitive information in logs
- **Severity**: INFO
- **CWE**: CWE-532 (Insertion of Sensitive Information into Log File)
- **Recommendation**: Remove or disable logging in production:
  ```java
  if (BuildConfig.DEBUG) {
      Log.d(TAG, "Debug info");
  }
  ```

**3. SQL Injection**
- **What**: Use of raw SQL queries
- **Risk**: SQL injection attacks
- **Detection**: `rawQuery`, `execSQL` usage
- **Recommendation**: Use parameterized queries or Room ORM

---

### M8: Code Tampering

Lack of protection against code modification.

#### Checks Performed:

**1. Root Detection**
- **What**: Presence of root detection mechanisms
- **Risk**: App runs on compromised devices
- **Severity**: INFO
- **Recommendation**: Implement root detection:
  ```java
  - Check for su binary
  - Check for root management apps
  - Verify SafetyNet attestation
  ```

**2. Integrity Checks**
- **What**: App signature verification
- **Risk**: Modified/repackaged apps
- **Recommendation**: Implement runtime integrity checks

---

### M9: Reverse Engineering

Lack of protection against reverse engineering.

#### Checks Performed:

**1. Code Obfuscation**
- **What**: Detection of obfuscation (ProGuard/R8)
- **Risk**: Easier reverse engineering
- **Detection**: Checks for obfuscated class names
- **Severity**: MEDIUM
- **Recommendation**: Enable ProGuard/R8:
  ```groovy
  buildTypes {
      release {
          minifyEnabled true
          proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')
      }
  }
  ```

**2. Native Library Protection**
- **What**: Presence and protection of native libraries
- **Risk**: Reverse engineering of native code
- **Recommendation**: Use native code obfuscation, anti-debugging

---

### M10: Extraneous Functionality

Hidden backdoors or test code in production.

#### Checks Performed:

**1. Test Code Detection**
- **What**: Test endpoints or debug features in production
- **Risk**: Unauthorized access to debug functionality
- **Recommendation**: Remove all test code from production builds

**2. Backdoor Detection**
- **What**: Hidden access points
- **Risk**: Unauthorized access
- **Recommendation**: Code review, remove all debug/test features

---

## Severity Levels

| Level | Description | Action Required |
|-------|-------------|-----------------|
| **CRITICAL** | Immediate security risk | Fix immediately before release |
| **HIGH** | Serious security vulnerability | Fix before production release |
| **MEDIUM** | Moderate security concern | Fix in next release cycle |
| **LOW** | Minor security improvement | Consider for future releases |
| **INFO** | Informational, best practice | Review and consider |

---

## CWE References

Common Weakness Enumeration (CWE) references used:

- **CWE-200**: Exposure of Sensitive Information
- **CWE-312**: Cleartext Storage of Sensitive Information
- **CWE-319**: Cleartext Transmission of Sensitive Information
- **CWE-489**: Active Debug Code
- **CWE-532**: Insertion of Sensitive Information into Log File
- **CWE-798**: Use of Hard-coded Credentials
- **CWE-926**: Improper Export of Android Application Components
- **CWE-927**: Use of Implicit Intent for Sensitive Communication

---

## Additional Resources

- [OWASP Mobile Top 10](https://owasp.org/www-project-mobile-top-10/)
- [OWASP MASVS](https://github.com/OWASP/owasp-masvs)
- [OWASP Mobile Security Testing Guide](https://owasp.org/www-project-mobile-security-testing-guide/)
- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)
- [CWE Database](https://cwe.mitre.org/)
