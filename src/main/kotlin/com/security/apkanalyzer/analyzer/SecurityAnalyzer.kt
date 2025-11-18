package com.security.apkanalyzer.analyzer

import com.security.apkanalyzer.domain.*
import com.security.apkanalyzer.detector.MalwareDetector
import com.security.apkanalyzer.detector.OwaspChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SecurityAnalyzer {

    private val apkParser = ApkParser()
    private val dexAnalyzer = DexAnalyzer()
    private val malwareDetector = MalwareDetector()
    private val owaspChecker = OwaspChecker()

    suspend fun analyzeApk(file: File): AnalysisReport = withContext(Dispatchers.IO) {
        // Parse APK
        val apkInfo = when {
            file.extension.equals("apk", ignoreCase = true) -> apkParser.parseApk(file)
            file.extension.equals("aar", ignoreCase = true) -> apkParser.parseAar(file)
            else -> throw IllegalArgumentException("Unsupported file type: ${file.extension}")
        }

        // Analyze DEX files
        val dexClasses = if (file.extension.equals("apk", ignoreCase = true)) {
            dexAnalyzer.analyzeDexFiles(file)
        } else {
            emptyList()
        }

        // Detect malware
        val malwareDetection = malwareDetector.detectMalware(apkInfo, dexClasses)

        // Perform OWASP checks
        val owaspResults = owaspChecker.performOwaspChecks(apkInfo, dexClasses)

        // Calculate overall score
        val overallScore = calculateOverallScore(malwareDetection, owaspResults)

        // Determine risk level
        val riskLevel = determineRiskLevel(overallScore, malwareDetection)

        AnalysisReport(
            apkInfo = apkInfo,
            malwareDetection = malwareDetection,
            owaspResults = owaspResults,
            overallScore = overallScore,
            riskLevel = riskLevel
        )
    }

    private fun calculateOverallScore(
        malwareDetection: MalwareDetectionResult,
        owaspResults: List<OwaspCheckResult>
    ): Int {
        // Start with 100
        var score = 100

        // Deduct based on malware detection
        if (malwareDetection.isMalicious) {
            score -= (malwareDetection.confidence * 50).toInt()
        }

        // Deduct based on malware findings
        malwareDetection.findings.forEach { finding ->
            score -= when (finding.level) {
                SecurityLevel.CRITICAL -> 15
                SecurityLevel.HIGH -> 10
                SecurityLevel.MEDIUM -> 5
                SecurityLevel.LOW -> 2
                SecurityLevel.INFO -> 1
            }
        }

        // Average OWASP scores
        val owaspAverage = if (owaspResults.isNotEmpty()) {
            owaspResults.map { it.score }.average().toInt()
        } else {
            100
        }

        // Combine scores (60% malware, 40% OWASP)
        val finalScore = (score * 0.6 + owaspAverage * 0.4).toInt()

        return finalScore.coerceIn(0, 100)
    }

    private fun determineRiskLevel(score: Int, malwareDetection: MalwareDetectionResult): SecurityLevel {
        return when {
            malwareDetection.isMalicious && malwareDetection.confidence > 0.8f -> SecurityLevel.CRITICAL
            score < 30 -> SecurityLevel.CRITICAL
            score < 50 -> SecurityLevel.HIGH
            score < 70 -> SecurityLevel.MEDIUM
            score < 90 -> SecurityLevel.LOW
            else -> SecurityLevel.INFO
        }
    }
}
