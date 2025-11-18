package com.security.apkanalyzer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.security.apkanalyzer.domain.AnalysisReport
import com.security.apkanalyzer.domain.SecurityLevel

@Composable
fun OverviewTab(report: AnalysisReport) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Security Score Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (report.riskLevel) {
                    SecurityLevel.CRITICAL -> Color(0xFFFFEBEE)
                    SecurityLevel.HIGH -> Color(0xFFFFF3E0)
                    SecurityLevel.MEDIUM -> Color(0xFFFFFDE7)
                    SecurityLevel.LOW -> Color(0xFFE8F5E9)
                    SecurityLevel.INFO -> Color(0xFFE3F2FD)
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = report.overallScore / 100f,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 12.dp,
                        color = when (report.riskLevel) {
                            SecurityLevel.CRITICAL -> Color(0xFFD32F2F)
                            SecurityLevel.HIGH -> Color(0xFFF57C00)
                            SecurityLevel.MEDIUM -> Color(0xFFFBC02D)
                            SecurityLevel.LOW -> Color(0xFF388E3C)
                            SecurityLevel.INFO -> Color(0xFF1976D2)
                        }
                    )
                    Text(
                        "${report.overallScore}",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Column {
                    Text(
                        "Security Score",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "Risk Level: ${report.riskLevel.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = when (report.riskLevel) {
                            SecurityLevel.CRITICAL -> Color(0xFFD32F2F)
                            SecurityLevel.HIGH -> Color(0xFFF57C00)
                            SecurityLevel.MEDIUM -> Color(0xFFFBC02D)
                            SecurityLevel.LOW -> Color(0xFF388E3C)
                            SecurityLevel.INFO -> Color(0xFF1976D2)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (report.malwareDetection.isMalicious) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "MALWARE DETECTED (${(report.malwareDetection.confidence * 100).toInt()}% confidence)",
                                color = Color(0xFFD32F2F),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // APK Info Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("APK Information", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                InfoRow("Package Name", report.apkInfo.packageName)
                InfoRow("Version", "${report.apkInfo.versionName} (${report.apkInfo.versionCode})")
                InfoRow("Min SDK", report.apkInfo.minSdkVersion.toString())
                InfoRow("Target SDK", report.apkInfo.targetSdkVersion.toString())
                InfoRow("Permissions", report.apkInfo.permissions.size.toString())
                InfoRow("Activities", report.apkInfo.activities.size.toString())
                InfoRow("Services", report.apkInfo.services.size.toString())
                InfoRow("Receivers", report.apkInfo.receivers.size.toString())
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Malware Findings",
                value = report.malwareDetection.findings.size.toString(),
                icon = Icons.Default.BugReport,
                color = if (report.malwareDetection.isMalicious) Color(0xFFD32F2F) else Color(0xFF388E3C)
            )

            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "OWASP Issues",
                value = report.owaspResults.sumOf { it.findings.size }.toString(),
                icon = Icons.Default.Security,
                color = Color(0xFFF57C00)
            )

            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Network Connections",
                value = report.malwareDetection.networkConnections.size.toString(),
                icon = Icons.Default.Cloud,
                color = Color(0xFF1976D2)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detected Malware Types
        if (report.malwareDetection.detectedMalwareTypes.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Detected Malware Types",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFD32F2F)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    report.malwareDetection.detectedMalwareTypes.forEach { type ->
                        Text("• ${type.name}", color = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            "$label:",
            modifier = Modifier.width(150.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(48.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
