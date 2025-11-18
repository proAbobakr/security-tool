package com.security.apkanalyzer.domain

import java.io.File

data class ApkInfo(
    val file: File,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val minSdkVersion: Int,
    val targetSdkVersion: Int,
    val permissions: List<String>,
    val activities: List<String>,
    val services: List<String>,
    val receivers: List<String>,
    val providers: List<String>,
    val usesLibraries: List<String>,
    val certificates: List<CertificateInfo>
)

data class CertificateInfo(
    val issuer: String,
    val subject: String,
    val serialNumber: String,
    val validFrom: String,
    val validTo: String,
    val signatureAlgorithm: String
)

enum class SecurityLevel {
    CRITICAL, HIGH, MEDIUM, LOW, INFO
}

data class SecurityFinding(
    val id: String,
    val title: String,
    val description: String,
    val level: SecurityLevel,
    val category: FindingCategory,
    val recommendation: String,
    val affectedFiles: List<String> = emptyList(),
    val codeSnippet: String? = null
)

enum class FindingCategory {
    MALWARE,
    OWASP_M1_IMPROPER_PLATFORM_USAGE,
    OWASP_M2_INSECURE_DATA_STORAGE,
    OWASP_M3_INSECURE_COMMUNICATION,
    OWASP_M4_INSECURE_AUTHENTICATION,
    OWASP_M5_INSUFFICIENT_CRYPTOGRAPHY,
    OWASP_M6_INSECURE_AUTHORIZATION,
    OWASP_M7_CLIENT_CODE_QUALITY,
    OWASP_M8_CODE_TAMPERING,
    OWASP_M9_REVERSE_ENGINEERING,
    OWASP_M10_EXTRANEOUS_FUNCTIONALITY,
    PRIVACY,
    GENERAL_SECURITY
}

data class MalwareDetectionResult(
    val isMalicious: Boolean,
    val confidence: Float, // 0.0 to 1.0
    val detectedMalwareTypes: List<MalwareType>,
    val findings: List<SecurityFinding>,
    val suspiciousApis: List<SuspiciousApi>,
    val networkConnections: List<NetworkConnection>
)

enum class MalwareType {
    TROJAN,
    SPYWARE,
    ADWARE,
    RANSOMWARE,
    ROOTKIT,
    BACKDOOR,
    KEYLOGGER,
    BANKING_MALWARE,
    SMS_FRAUD,
    UNKNOWN
}

data class SuspiciousApi(
    val apiName: String,
    val className: String,
    val methodName: String,
    val description: String,
    val riskLevel: SecurityLevel
)

data class NetworkConnection(
    val url: String,
    val protocol: String,
    val purpose: String
)

data class OwaspCheckResult(
    val category: FindingCategory,
    val passed: Boolean,
    val findings: List<SecurityFinding>,
    val score: Int // 0-100
)

data class AnalysisReport(
    val apkInfo: ApkInfo,
    val malwareDetection: MalwareDetectionResult,
    val owaspResults: List<OwaspCheckResult>,
    val overallScore: Int, // 0-100
    val riskLevel: SecurityLevel,
    val analyzedAt: Long = System.currentTimeMillis()
)

data class DexClass(
    val className: String,
    val methods: List<DexMethod>,
    val fields: List<DexField>,
    val superClass: String?,
    val interfaces: List<String>
)

data class DexMethod(
    val name: String,
    val descriptor: String,
    val accessFlags: Int,
    val code: String?
)

data class DexField(
    val name: String,
    val type: String,
    val accessFlags: Int
)

data class InstrumentationHook(
    val targetClass: String,
    val targetMethod: String,
    val hookType: HookType,
    val hookCode: String,
    val enabled: Boolean = true
)

enum class HookType {
    BEFORE,
    AFTER,
    REPLACE
}
