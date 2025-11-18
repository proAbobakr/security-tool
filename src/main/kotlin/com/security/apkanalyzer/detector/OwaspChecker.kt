package com.security.apkanalyzer.detector

import com.security.apkanalyzer.domain.*
import java.io.File

class OwaspChecker {

    fun performOwaspChecks(apkInfo: ApkInfo, dexClasses: List<DexClass>): List<OwaspCheckResult> {
        return listOf(
            checkM1ImproperPlatformUsage(apkInfo, dexClasses),
            checkM2InsecureDataStorage(dexClasses),
            checkM3InsecureCommunication(dexClasses),
            checkM4InsecureAuthentication(dexClasses),
            checkM5InsufficientCryptography(dexClasses),
            checkM6InsecureAuthorization(apkInfo, dexClasses),
            checkM7ClientCodeQuality(dexClasses),
            checkM8CodeTampering(apkInfo),
            checkM9ReverseEngineering(apkInfo, dexClasses),
            checkM10ExtraneousFunctionality(dexClasses)
        )
    }

    // M1: Improper Platform Usage
    private fun checkM1ImproperPlatformUsage(apkInfo: ApkInfo, dexClasses: List<DexClass>): OwaspCheckResult {
        val findings = mutableListOf<SecurityFinding>()

        // Check for exported components without permission
        if (apkInfo.activities.isNotEmpty() || apkInfo.services.isNotEmpty() || apkInfo.receivers.isNotEmpty()) {
            findings.add(
                SecurityFinding(
                    id = "M1_EXPORTED_COMPONENTS",
                    title = "Exported Components Detected",
                    description = "App has exported components that may be accessible to other apps",
                    level = SecurityLevel.MEDIUM,
                    category = FindingCategory.OWASP_M1_IMPROPER_PLATFORM_USAGE,
                    recommendation = "Ensure exported components are properly protected with permissions"
                )
            )
        }

        // Check for improper WebView usage
        dexClasses.forEach { dexClass ->
            dexClass.methods.forEach { method ->
                if (method.code?.contains("WebView") == true &&
                    method.code?.contains("setJavaScriptEnabled(true)") == true
                ) {
                    findings.add(
                        SecurityFinding(
                            id = "M1_WEBVIEW_JAVASCRIPT",
                            title = "Unsafe WebView Configuration",
                            description = "WebView has JavaScript enabled which can lead to XSS attacks",
                            level = SecurityLevel.HIGH,
                            category = FindingCategory.OWASP_M1_IMPROPER_PLATFORM_USAGE,
                            recommendation = "Disable JavaScript in WebView unless absolutely necessary",
                            affectedFiles = listOf(dexClass.className)
                        )
                    )
                }
            }
        }

        val score = calculateScore(findings)
        return OwaspCheckResult(
            category = FindingCategory.OWASP_M1_IMPROPER_PLATFORM_USAGE,
            passed = findings.isEmpty(),
            findings = findings,
            score = score
        )
    }

    // M2: Insecure Data Storage
    private fun checkM2InsecureDataStorage(dexClasses: List<DexClass>): OwaspCheckResult {
        val findings = mutableListOf<SecurityFinding>()

        val insecureStoragePatterns = mapOf(
            "SharedPreferences" to "SharedPreferences without encryption",
            "MODE_WORLD_READABLE" to "World-readable file mode",
            "MODE_WORLD_WRITEABLE" to "World-writable file mode",
            "openFileOutput" to "Unencrypted file output",
            "SQLiteDatabase" to "Unencrypted database",
            "getExternalStorageDirectory" to "External storage usage"
        )

        dexClasses.forEach { dexClass ->
            dexClass.methods.forEach { method ->
                insecureStoragePatterns.forEach { (pattern, description) ->
                    if (method.code?.contains(pattern) == true) {
                        findings.add(
                            SecurityFinding(
                                id = "M2_INSECURE_STORAGE_$pattern",
                                title = "Insecure Data Storage: $description",
                                description = "Found usage of $pattern which may store data insecurely",
                                level = when (pattern) {
                                    "MODE_WORLD_READABLE", "MODE_WORLD_WRITEABLE" -> SecurityLevel.CRITICAL
                                    else -> SecurityLevel.HIGH
                                },
                                category = FindingCategory.OWASP_M2_INSECURE_DATA_STORAGE,
                                recommendation = "Use encrypted storage (EncryptedSharedPreferences, SQLCipher) for sensitive data",
                                affectedFiles = listOf("${dexClass.className}.${method.name}")
                            )
                        )
                    }
                }
            }
        }

        val score = calculateScore(findings)
        return OwaspCheckResult(
            category = FindingCategory.OWASP_M2_INSECURE_DATA_STORAGE,
            passed = findings.isEmpty(),
            findings = findings,
            score = score
        )
    }

    // M3: Insecure Communication
    private fun checkM3InsecureCommunication(dexClasses: List<DexClass>): OwaspCheckResult {
        val findings = mutableListOf<SecurityFinding>()

        dexClasses.forEach { dexClass ->
            dexClass.methods.forEach { method ->
                method.code?.let { code ->
                    // Check for HTTP usage
                    if (code.contains("http://") && !code.contains("https://")) {
                        findings.add(
                            SecurityFinding(
                                id = "M3_HTTP_USAGE",
                                title = "Insecure HTTP Communication",
                                description = "App uses unencrypted HTTP connections",
                                level = SecurityLevel.HIGH,
                                category = FindingCategory.OWASP_M3_INSECURE_COMMUNICATION,
                                recommendation = "Use HTTPS for all network communications",
                                affectedFiles = listOf(dexClass.className)
                            )
                        )
                    }

                    // Check for SSL certificate validation bypass
                    if (code.contains("TrustAllCertificates") ||
                        code.contains("X509TrustManager") ||
                        code.contains("HostnameVerifier")
                    ) {
                        findings.add(
                            SecurityFinding(
                                id = "M3_SSL_BYPASS",
                                title = "SSL Certificate Validation Bypass",
                                description = "App may bypass SSL certificate validation",
                                level = SecurityLevel.CRITICAL,
                                category = FindingCategory.OWASP_M3_INSECURE_COMMUNICATION,
                                recommendation = "Never bypass SSL certificate validation",
                                affectedFiles = listOf("${dexClass.className}.${method.name}")
                            )
                        )
                    }
                }
            }
        }

        val score = calculateScore(findings)
        return OwaspCheckResult(
            category = FindingCategory.OWASP_M3_INSECURE_COMMUNICATION,
            passed = findings.isEmpty(),
            findings = findings,
            score = score
        )
    }

    // M4: Insecure Authentication
    private fun checkM4InsecureAuthentication(dexClasses: List<DexClass>): OwaspCheckResult {
        val findings = mutableListOf<SecurityFinding>()

        dexClasses.forEach { dexClass ->
            dexClass.methods.forEach { method ->
                method.code?.let { code ->
                    // Check for hardcoded credentials
                    val passwordPatterns = listOf(
                        Regex("""password\s*=\s*["'][^"']+["']""", RegexOption.IGNORE_CASE),
                        Regex("""api[_-]?key\s*=\s*["'][^"']+["']""", RegexOption.IGNORE_CASE),
                        Regex("""secret\s*=\s*["'][^"']+["']""", RegexOption.IGNORE_CASE)
                    )

                    passwordPatterns.forEach { pattern ->
                        if (pattern.containsMatchIn(code)) {
                            findings.add(
                                SecurityFinding(
                                    id = "M4_HARDCODED_CREDENTIALS",
                                    title = "Hardcoded Credentials",
                                    description = "Found hardcoded credentials in code",
                                    level = SecurityLevel.CRITICAL,
                                    category = FindingCategory.OWASP_M4_INSECURE_AUTHENTICATION,
                                    recommendation = "Never hardcode credentials; use secure credential storage",
                                    affectedFiles = listOf("${dexClass.className}.${method.name}")
                                )
                            )
                        }
                    }

                    // Check for weak authentication
                    if (code.contains("equals") && (code.contains("password") || code.contains("pin"))) {
                        findings.add(
                            SecurityFinding(
                                id = "M4_WEAK_AUTH",
                                title = "Potentially Weak Authentication",
                                description = "Authentication implementation may be weak",
                                level = SecurityLevel.MEDIUM,
                                category = FindingCategory.OWASP_M4_INSECURE_AUTHENTICATION,
                                recommendation = "Use secure authentication mechanisms and never compare passwords with simple equals",
                                affectedFiles = listOf("${dexClass.className}.${method.name}")
                            )
                        )
                    }
                }
            }
        }

        val score = calculateScore(findings)
        return OwaspCheckResult(
            category = FindingCategory.OWASP_M4_INSECURE_AUTHENTICATION,
            passed = findings.isEmpty(),
            findings = findings,
            score = score
        )
    }

    // M5: Insufficient Cryptography
    private fun checkM5InsufficientCryptography(dexClasses: List<DexClass>): OwaspCheckResult {
        val findings = mutableListOf<SecurityFinding>()

        val weakCryptoPatterns = mapOf(
            "DES" to "Weak DES encryption",
            "MD5" to "Weak MD5 hashing",
            "SHA1" to "Weak SHA-1 hashing",
            "ECB" to "Insecure ECB mode",
            "Random()" to "Weak random number generation"
        )

        dexClasses.forEach { dexClass ->
            dexClass.methods.forEach { method ->
                weakCryptoPatterns.forEach { (pattern, description) ->
                    if (method.code?.contains(pattern) == true) {
                        findings.add(
                            SecurityFinding(
                                id = "M5_WEAK_CRYPTO_$pattern",
                                title = "Weak Cryptography: $description",
                                description = "Found usage of weak cryptographic algorithm: $pattern",
                                level = when (pattern) {
                                    "DES", "MD5" -> SecurityLevel.CRITICAL
                                    else -> SecurityLevel.HIGH
                                },
                                category = FindingCategory.OWASP_M5_INSUFFICIENT_CRYPTOGRAPHY,
                                recommendation = "Use strong cryptographic algorithms: AES-256, SHA-256, SecureRandom",
                                affectedFiles = listOf("${dexClass.className}.${method.name}")
                            )
                        )
                    }
                }
            }
        }

        val score = calculateScore(findings)
        return OwaspCheckResult(
            category = FindingCategory.OWASP_M5_INSUFFICIENT_CRYPTOGRAPHY,
            passed = findings.isEmpty(),
            findings = findings,
            score = score
        )
    }

    // M6: Insecure Authorization
    private fun checkM6InsecureAuthorization(apkInfo: ApkInfo, dexClasses: List<DexClass>): OwaspCheckResult {
        val findings = mutableListOf<SecurityFinding>()

        // Check for permission-based authorization issues
        val sensitivePermissions = listOf(
            "android.permission.WRITE_SECURE_SETTINGS",
            "android.permission.INSTALL_PACKAGES",
            "android.permission.DELETE_PACKAGES"
        )

        apkInfo.permissions.filter { it in sensitivePermissions }.forEach { permission ->
            findings.add(
                SecurityFinding(
                    id = "M6_SENSITIVE_PERMISSION",
                    title = "Sensitive Permission Requested",
                    description = "App requests sensitive permission: $permission",
                    level = SecurityLevel.HIGH,
                    category = FindingCategory.OWASP_M6_INSECURE_AUTHORIZATION,
                    recommendation = "Ensure proper authorization checks are in place"
                )
            )
        }

        val score = calculateScore(findings)
        return OwaspCheckResult(
            category = FindingCategory.OWASP_M6_INSECURE_AUTHORIZATION,
            passed = findings.isEmpty(),
            findings = findings,
            score = score
        )
    }

    // M7: Client Code Quality
    private fun checkM7ClientCodeQuality(dexClasses: List<DexClass>): OwaspCheckResult {
        val findings = mutableListOf<SecurityFinding>()

        dexClasses.forEach { dexClass ->
            dexClass.methods.forEach { method ->
                method.code?.let { code ->
                    // Check for buffer overflows
                    if (code.contains("BufferOverflow") || code.contains("ArrayIndexOutOfBounds")) {
                        findings.add(
                            SecurityFinding(
                                id = "M7_BUFFER_OVERFLOW",
                                title = "Potential Buffer Overflow",
                                description = "Code may be vulnerable to buffer overflow",
                                level = SecurityLevel.HIGH,
                                category = FindingCategory.OWASP_M7_CLIENT_CODE_QUALITY,
                                recommendation = "Implement proper bounds checking",
                                affectedFiles = listOf("${dexClass.className}.${method.name}")
                            )
                        )
                    }

                    // Check for SQL injection
                    if (code.contains("rawQuery") || code.contains("execSQL")) {
                        findings.add(
                            SecurityFinding(
                                id = "M7_SQL_INJECTION",
                                title = "Potential SQL Injection",
                                description = "Raw SQL queries may be vulnerable to injection",
                                level = SecurityLevel.HIGH,
                                category = FindingCategory.OWASP_M7_CLIENT_CODE_QUALITY,
                                recommendation = "Use parameterized queries",
                                affectedFiles = listOf("${dexClass.className}.${method.name}")
                            )
                        )
                    }
                }
            }
        }

        val score = calculateScore(findings)
        return OwaspCheckResult(
            category = FindingCategory.OWASP_M7_CLIENT_CODE_QUALITY,
            passed = findings.isEmpty(),
            findings = findings,
            score = score
        )
    }

    // M8: Code Tampering
    private fun checkM8CodeTampering(apkInfo: ApkInfo): OwaspCheckResult {
        val findings = mutableListOf<SecurityFinding>()

        // Check for debug mode
        // Note: This would require parsing AndroidManifest.xml more thoroughly
        findings.add(
            SecurityFinding(
                id = "M8_NO_TAMPERING_DETECTION",
                title = "No Code Tampering Detection",
                description = "App should implement runtime integrity checks",
                level = SecurityLevel.MEDIUM,
                category = FindingCategory.OWASP_M8_CODE_TAMPERING,
                recommendation = "Implement code integrity checks and anti-tampering measures"
            )
        )

        val score = calculateScore(findings)
        return OwaspCheckResult(
            category = FindingCategory.OWASP_M8_CODE_TAMPERING,
            passed = findings.isEmpty(),
            findings = findings,
            score = score
        )
    }

    // M9: Reverse Engineering
    private fun checkM9ReverseEngineering(apkInfo: ApkInfo, dexClasses: List<DexClass>): OwaspCheckResult {
        val findings = mutableListOf<SecurityFinding>()

        // Check if code is obfuscated
        val shortNamedClasses = dexClasses.count { dexClass ->
            val className = dexClass.className.split("/").lastOrNull() ?: ""
            className.length <= 2
        }

        val obfuscationRatio = shortNamedClasses.toFloat() / dexClasses.size

        if (obfuscationRatio < 0.1f) {
            findings.add(
                SecurityFinding(
                    id = "M9_NO_OBFUSCATION",
                    title = "Code Not Obfuscated",
                    description = "App code is not obfuscated, making reverse engineering easier",
                    level = SecurityLevel.MEDIUM,
                    category = FindingCategory.OWASP_M9_REVERSE_ENGINEERING,
                    recommendation = "Use ProGuard/R8 to obfuscate code"
                )
            )
        }

        // Check for root/emulator detection
        val hasRootDetection = dexClasses.any { dexClass ->
            dexClass.methods.any { method ->
                method.code?.contains("su") == true ||
                        method.code?.contains("Superuser") == true
            }
        }

        if (!hasRootDetection) {
            findings.add(
                SecurityFinding(
                    id = "M9_NO_ROOT_DETECTION",
                    title = "No Root Detection",
                    description = "App should detect rooted devices to prevent analysis",
                    level = SecurityLevel.LOW,
                    category = FindingCategory.OWASP_M9_REVERSE_ENGINEERING,
                    recommendation = "Implement root and emulator detection"
                )
            )
        }

        val score = calculateScore(findings)
        return OwaspCheckResult(
            category = FindingCategory.OWASP_M9_REVERSE_ENGINEERING,
            passed = findings.isEmpty(),
            findings = findings,
            score = score
        )
    }

    // M10: Extraneous Functionality
    private fun checkM10ExtraneousFunctionality(dexClasses: List<DexClass>): OwaspCheckResult {
        val findings = mutableListOf<SecurityFinding>()

        val debugPatterns = listOf("Log.d", "Log.v", "System.out.println", "printStackTrace")

        dexClasses.forEach { dexClass ->
            dexClass.methods.forEach { method ->
                debugPatterns.forEach { pattern ->
                    if (method.code?.contains(pattern) == true) {
                        findings.add(
                            SecurityFinding(
                                id = "M10_DEBUG_CODE",
                                title = "Debug Code in Production",
                                description = "Found debug logging statements: $pattern",
                                level = SecurityLevel.LOW,
                                category = FindingCategory.OWASP_M10_EXTRANEOUS_FUNCTIONALITY,
                                recommendation = "Remove debug code from production builds",
                                affectedFiles = listOf("${dexClass.className}.${method.name}")
                            )
                        )
                    }
                }
            }
        }

        val score = calculateScore(findings)
        return OwaspCheckResult(
            category = FindingCategory.OWASP_M10_EXTRANEOUS_FUNCTIONALITY,
            passed = findings.isEmpty(),
            findings = findings,
            score = score
        )
    }

    private fun calculateScore(findings: List<SecurityFinding>): Int {
        if (findings.isEmpty()) return 100

        var deductions = 0
        findings.forEach { finding ->
            deductions += when (finding.level) {
                SecurityLevel.CRITICAL -> 30
                SecurityLevel.HIGH -> 20
                SecurityLevel.MEDIUM -> 10
                SecurityLevel.LOW -> 5
                SecurityLevel.INFO -> 2
            }
        }

        return (100 - deductions).coerceAtLeast(0)
    }
}
