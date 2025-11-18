package com.security.apkanalyzer.utils

import com.security.apkanalyzer.domain.AnalysisReport
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ReportExporter {

    fun exportToHtml(report: AnalysisReport, outputFile: File) {
        val html = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang='en'>")
            appendLine("<head>")
            appendLine("  <meta charset='UTF-8'>")
            appendLine("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>")
            appendLine("  <title>APK Security Analysis Report</title>")
            appendLine("  <style>")
            appendLine(getCssStyles())
            appendLine("  </style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("  <div class='container'>")
            appendLine("    <h1>APK Security Analysis Report</h1>")
            appendLine("    <p class='timestamp'>Generated: ${formatTimestamp(report.analyzedAt)}</p>")

            // Overview section
            appendLine("    <div class='section'>")
            appendLine("      <h2>Overview</h2>")
            appendLine("      <div class='score-badge score-${getRiskClass(report.riskLevel.name)}'>")
            appendLine("        <div class='score'>${report.overallScore}</div>")
            appendLine("        <div class='risk-level'>Risk: ${report.riskLevel.name}</div>")
            appendLine("      </div>")
            appendLine("    </div>")

            // APK Info
            appendLine("    <div class='section'>")
            appendLine("      <h2>APK Information</h2>")
            appendLine("      <table>")
            appendLine("        <tr><td>Package Name</td><td>${report.apkInfo.packageName}</td></tr>")
            appendLine("        <tr><td>Version</td><td>${report.apkInfo.versionName} (${report.apkInfo.versionCode})</td></tr>")
            appendLine("        <tr><td>Min SDK</td><td>${report.apkInfo.minSdkVersion}</td></tr>")
            appendLine("        <tr><td>Target SDK</td><td>${report.apkInfo.targetSdkVersion}</td></tr>")
            appendLine("      </table>")
            appendLine("    </div>")

            // Malware Detection
            appendLine("    <div class='section'>")
            appendLine("      <h2>Malware Detection</h2>")
            appendLine("      <div class='alert ${if (report.malwareDetection.isMalicious) "alert-danger" else "alert-success"}'>")
            appendLine("        <strong>${if (report.malwareDetection.isMalicious) "⚠️ MALWARE DETECTED" else "✓ No Malware Detected"}</strong>")
            appendLine("        <br>Confidence: ${(report.malwareDetection.confidence * 100).toInt()}%")
            appendLine("      </div>")

            if (report.malwareDetection.findings.isNotEmpty()) {
                appendLine("      <h3>Findings</h3>")
                report.malwareDetection.findings.forEach { finding ->
                    appendLine("      <div class='finding finding-${finding.level.name.lowercase()}'>")
                    appendLine("        <h4>${finding.title} <span class='badge'>${finding.level.name}</span></h4>")
                    appendLine("        <p>${finding.description}</p>")
                    appendLine("        <p class='recommendation'><strong>Recommendation:</strong> ${finding.recommendation}</p>")
                    appendLine("      </div>")
                }
            }
            appendLine("    </div>")

            // OWASP Results
            appendLine("    <div class='section'>")
            appendLine("      <h2>OWASP Mobile Top 10 Results</h2>")
            report.owaspResults.forEach { result ->
                val status = if (result.passed) "✓" else "✗"
                appendLine("      <div class='owasp-check ${if (result.passed) "passed" else "failed"}'>")
                appendLine("        <h3>$status ${getCategoryName(result.category)} - Score: ${result.score}/100</h3>")
                if (result.findings.isNotEmpty()) {
                    result.findings.forEach { finding ->
                        appendLine("        <div class='sub-finding'>")
                        appendLine("          <strong>${finding.title}</strong>: ${finding.description}")
                        appendLine("        </div>")
                    }
                }
                appendLine("      </div>")
            }
            appendLine("    </div>")

            appendLine("  </div>")
            appendLine("</body>")
            appendLine("</html>")
        }

        outputFile.writeText(html)
    }

    fun exportToJson(report: AnalysisReport, outputFile: File) {
        val json = buildString {
            appendLine("{")
            appendLine("  \"analyzedAt\": \"${formatTimestamp(report.analyzedAt)}\",")
            appendLine("  \"overallScore\": ${report.overallScore},")
            appendLine("  \"riskLevel\": \"${report.riskLevel.name}\",")
            appendLine("  \"apkInfo\": {")
            appendLine("    \"packageName\": \"${report.apkInfo.packageName}\",")
            appendLine("    \"versionName\": \"${report.apkInfo.versionName}\",")
            appendLine("    \"versionCode\": ${report.apkInfo.versionCode},")
            appendLine("    \"minSdkVersion\": ${report.apkInfo.minSdkVersion},")
            appendLine("    \"targetSdkVersion\": ${report.apkInfo.targetSdkVersion}")
            appendLine("  },")
            appendLine("  \"malwareDetection\": {")
            appendLine("    \"isMalicious\": ${report.malwareDetection.isMalicious},")
            appendLine("    \"confidence\": ${report.malwareDetection.confidence},")
            appendLine("    \"findingsCount\": ${report.malwareDetection.findings.size}")
            appendLine("  },")
            appendLine("  \"owaspResults\": {")
            appendLine("    \"totalChecks\": ${report.owaspResults.size},")
            appendLine("    \"passed\": ${report.owaspResults.count { it.passed }}")
            appendLine("  }")
            appendLine("}")
        }

        outputFile.writeText(json)
    }

    private fun getCssStyles(): String {
        return """
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                line-height: 1.6;
                color: #333;
                background: #f5f5f5;
                margin: 0;
                padding: 20px;
            }
            .container {
                max-width: 1000px;
                margin: 0 auto;
                background: white;
                padding: 40px;
                border-radius: 8px;
                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            }
            h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }
            h2 { color: #34495e; margin-top: 30px; }
            h3 { color: #7f8c8d; }
            .timestamp { color: #95a5a6; font-size: 14px; }
            .section { margin: 30px 0; }
            .score-badge {
                display: inline-block;
                padding: 20px;
                border-radius: 8px;
                text-align: center;
                min-width: 150px;
            }
            .score { font-size: 48px; font-weight: bold; }
            .risk-level { font-size: 16px; margin-top: 10px; }
            .score-critical { background: #ffebee; color: #c62828; }
            .score-high { background: #fff3e0; color: #e65100; }
            .score-medium { background: #fffde7; color: #f57f17; }
            .score-low { background: #e8f5e9; color: #2e7d32; }
            .score-info { background: #e3f2fd; color: #1565c0; }
            table { width: 100%; border-collapse: collapse; margin: 20px 0; }
            table td { padding: 12px; border-bottom: 1px solid #ecf0f1; }
            table td:first-child { font-weight: bold; width: 200px; }
            .alert {
                padding: 15px;
                border-radius: 4px;
                margin: 15px 0;
            }
            .alert-danger { background: #ffebee; color: #c62828; border-left: 4px solid #c62828; }
            .alert-success { background: #e8f5e9; color: #2e7d32; border-left: 4px solid #2e7d32; }
            .finding {
                margin: 15px 0;
                padding: 15px;
                border-radius: 4px;
                border-left: 4px solid;
            }
            .finding-critical { background: #ffebee; border-color: #c62828; }
            .finding-high { background: #fff3e0; border-color: #e65100; }
            .finding-medium { background: #fffde7; border-color: #f57f17; }
            .finding-low { background: #e8f5e9; border-color: #2e7d32; }
            .badge {
                display: inline-block;
                padding: 4px 8px;
                border-radius: 3px;
                font-size: 12px;
                background: #ecf0f1;
                color: #2c3e50;
            }
            .recommendation { font-style: italic; color: #7f8c8d; }
            .owasp-check {
                margin: 15px 0;
                padding: 15px;
                border-radius: 4px;
                background: #f8f9fa;
            }
            .owasp-check.passed { border-left: 4px solid #2e7d32; }
            .owasp-check.failed { border-left: 4px solid #e65100; }
            .sub-finding { margin: 10px 0; padding: 10px; background: white; border-radius: 4px; }
        """.trimIndent()
    }

    private fun formatTimestamp(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    }

    private fun getRiskClass(riskLevel: String): String {
        return riskLevel.lowercase()
    }

    private fun getCategoryName(category: com.security.apkanalyzer.domain.FindingCategory): String {
        return when (category) {
            com.security.apkanalyzer.domain.FindingCategory.OWASP_M1_IMPROPER_PLATFORM_USAGE -> "M1: Improper Platform Usage"
            com.security.apkanalyzer.domain.FindingCategory.OWASP_M2_INSECURE_DATA_STORAGE -> "M2: Insecure Data Storage"
            com.security.apkanalyzer.domain.FindingCategory.OWASP_M3_INSECURE_COMMUNICATION -> "M3: Insecure Communication"
            com.security.apkanalyzer.domain.FindingCategory.OWASP_M4_INSECURE_AUTHENTICATION -> "M4: Insecure Authentication"
            com.security.apkanalyzer.domain.FindingCategory.OWASP_M5_INSUFFICIENT_CRYPTOGRAPHY -> "M5: Insufficient Cryptography"
            com.security.apkanalyzer.domain.FindingCategory.OWASP_M6_INSECURE_AUTHORIZATION -> "M6: Insecure Authorization"
            com.security.apkanalyzer.domain.FindingCategory.OWASP_M7_CLIENT_CODE_QUALITY -> "M7: Client Code Quality"
            com.security.apkanalyzer.domain.FindingCategory.OWASP_M8_CODE_TAMPERING -> "M8: Code Tampering"
            com.security.apkanalyzer.domain.FindingCategory.OWASP_M9_REVERSE_ENGINEERING -> "M9: Reverse Engineering"
            com.security.apkanalyzer.domain.FindingCategory.OWASP_M10_EXTRANEOUS_FUNCTIONALITY -> "M10: Extraneous Functionality"
            else -> category.name
        }
    }
}
