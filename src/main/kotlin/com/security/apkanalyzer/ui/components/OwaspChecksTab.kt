package com.security.apkanalyzer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.security.apkanalyzer.domain.FindingCategory
import com.security.apkanalyzer.domain.OwaspCheckResult

@Composable
fun OwaspChecksTab(results: List<OwaspCheckResult>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Overall OWASP Score
            val overallScore = results.map { it.score }.average().toInt()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        overallScore >= 80 -> Color(0xFFE8F5E9)
                        overallScore >= 60 -> Color(0xFFFFFDE7)
                        overallScore >= 40 -> Color(0xFFFFF3E0)
                        else -> Color(0xFFFFEBEE)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = overallScore / 100f,
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 10.dp,
                            color = when {
                                overallScore >= 80 -> Color(0xFF388E3C)
                                overallScore >= 60 -> Color(0xFFFBC02D)
                                overallScore >= 40 -> Color(0xFFF57C00)
                                else -> Color(0xFFD32F2F)
                            }
                        )
                        Text(
                            "$overallScore",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    Column {
                        Text(
                            "OWASP Mobile Top 10 Score",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            "${results.count { it.passed }} of ${results.size} checks passed",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(results) { result ->
            OwaspCheckCard(result)
        }
    }
}

@Composable
fun OwaspCheckCard(result: OwaspCheckResult) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (result.passed) Icons.Default.CheckCircle else Icons.Default.Error,
                    null,
                    modifier = Modifier.size(32.dp),
                    tint = if (result.passed) Color(0xFF388E3C) else Color(0xFFF57C00)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        getCategoryName(result.category),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Score: ${result.score}/100 | ${result.findings.size} findings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        "Expand"
                    )
                }
            }

            if (expanded && result.findings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                result.findings.forEach { finding ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = getSecurityLevelColor(finding.level)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                finding.title,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            finding.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (finding.recommendation.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "→ ${finding.recommendation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (finding != result.findings.last()) {
                            Divider(modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

fun getCategoryName(category: FindingCategory): String {
    return when (category) {
        FindingCategory.OWASP_M1_IMPROPER_PLATFORM_USAGE -> "M1: Improper Platform Usage"
        FindingCategory.OWASP_M2_INSECURE_DATA_STORAGE -> "M2: Insecure Data Storage"
        FindingCategory.OWASP_M3_INSECURE_COMMUNICATION -> "M3: Insecure Communication"
        FindingCategory.OWASP_M4_INSECURE_AUTHENTICATION -> "M4: Insecure Authentication"
        FindingCategory.OWASP_M5_INSUFFICIENT_CRYPTOGRAPHY -> "M5: Insufficient Cryptography"
        FindingCategory.OWASP_M6_INSECURE_AUTHORIZATION -> "M6: Insecure Authorization"
        FindingCategory.OWASP_M7_CLIENT_CODE_QUALITY -> "M7: Client Code Quality"
        FindingCategory.OWASP_M8_CODE_TAMPERING -> "M8: Code Tampering"
        FindingCategory.OWASP_M9_REVERSE_ENGINEERING -> "M9: Reverse Engineering"
        FindingCategory.OWASP_M10_EXTRANEOUS_FUNCTIONALITY -> "M10: Extraneous Functionality"
        else -> category.name
    }
}
