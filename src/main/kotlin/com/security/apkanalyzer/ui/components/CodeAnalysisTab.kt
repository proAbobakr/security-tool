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
import androidx.compose.ui.unit.dp
import com.security.apkanalyzer.domain.ApkInfo

@Composable
fun CodeAnalysisTab(apkInfo: ApkInfo) {
    var selectedSection by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Section selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                label = { Text("Permissions") },
                leadingIcon = { Icon(Icons.Default.Security, null, modifier = Modifier.size(16.dp)) }
            )
            FilterChip(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                label = { Text("Components") },
                leadingIcon = { Icon(Icons.Default.Apps, null, modifier = Modifier.size(16.dp)) }
            )
            FilterChip(
                selected = selectedSection == 2,
                onClick = { selectedSection = 2 },
                label = { Text("Certificates") },
                leadingIcon = { Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(16.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedSection) {
            0 -> PermissionsSection(apkInfo.permissions)
            1 -> ComponentsSection(apkInfo)
            2 -> CertificatesSection(apkInfo.certificates)
        }
    }
}

@Composable
fun PermissionsSection(permissions: List<String>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "${permissions.size} Permissions",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            "Requested by this application",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(permissions) { permission ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        getPermissionIcon(permission),
                        null,
                        modifier = Modifier.size(24.dp),
                        tint = if (isDangerousPermission(permission))
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            permission.substringAfterLast("."),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            permission,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComponentsSection(apkInfo: ApkInfo) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Activities
        if (apkInfo.activities.isNotEmpty()) {
            item {
                ComponentCard(
                    title = "Activities (${apkInfo.activities.size})",
                    icon = Icons.Default.Apps,
                    components = apkInfo.activities
                )
            }
        }

        // Services
        if (apkInfo.services.isNotEmpty()) {
            item {
                ComponentCard(
                    title = "Services (${apkInfo.services.size})",
                    icon = Icons.Default.CloudQueue,
                    components = apkInfo.services
                )
            }
        }

        // Receivers
        if (apkInfo.receivers.isNotEmpty()) {
            item {
                ComponentCard(
                    title = "Broadcast Receivers (${apkInfo.receivers.size})",
                    icon = Icons.Default.Wifi,
                    components = apkInfo.receivers
                )
            }
        }

        // Providers
        if (apkInfo.providers.isNotEmpty()) {
            item {
                ComponentCard(
                    title = "Content Providers (${apkInfo.providers.size})",
                    icon = Icons.Default.Storage,
                    components = apkInfo.providers
                )
            }
        }
    }
}

@Composable
fun ComponentCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    components: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        "Expand"
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                components.forEach { component ->
                    Text(
                        component,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CertificatesSection(certificates: List<com.security.apkanalyzer.domain.CertificateInfo>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(certificates) { cert ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Certificate",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    CertInfoRow("Issuer", cert.issuer)
                    CertInfoRow("Subject", cert.subject)
                    CertInfoRow("Serial Number", cert.serialNumber)
                    CertInfoRow("Valid From", cert.validFrom)
                    CertInfoRow("Valid To", cert.validTo)
                    CertInfoRow("Signature Algorithm", cert.signatureAlgorithm)
                }
            }
        }

        if (certificates.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No certificate information available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CertInfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun getPermissionIcon(permission: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        permission.contains("CAMERA") -> Icons.Default.CameraAlt
        permission.contains("LOCATION") -> Icons.Default.LocationOn
        permission.contains("STORAGE") || permission.contains("WRITE") || permission.contains("READ") -> Icons.Default.Storage
        permission.contains("SMS") || permission.contains("SEND") -> Icons.Default.Sms
        permission.contains("CALL") || permission.contains("PHONE") -> Icons.Default.Phone
        permission.contains("INTERNET") || permission.contains("NETWORK") -> Icons.Default.Wifi
        permission.contains("CONTACTS") -> Icons.Default.Contacts
        permission.contains("AUDIO") || permission.contains("RECORD") -> Icons.Default.Mic
        else -> Icons.Default.Security
    }
}

fun isDangerousPermission(permission: String): Boolean {
    val dangerous = listOf(
        "READ_SMS", "SEND_SMS", "RECEIVE_SMS",
        "READ_CONTACTS", "WRITE_CONTACTS",
        "READ_CALL_LOG", "WRITE_CALL_LOG",
        "CAMERA", "RECORD_AUDIO",
        "ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION",
        "READ_PHONE_STATE", "CALL_PHONE",
        "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE"
    )
    return dangerous.any { permission.contains(it) }
}
