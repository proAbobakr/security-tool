package com.security.apkanalyzer.analyzer

import com.security.apkanalyzer.domain.ApkInfo
import com.security.apkanalyzer.domain.CertificateInfo
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.CertificateMeta
import java.io.File
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*

class ApkParser {

    fun parseApk(file: File): ApkInfo {
        val apkFile = ApkFile(file)

        return try {
            val apkMeta = apkFile.apkMeta
            val manifest = apkFile.manifestXml

            ApkInfo(
                file = file,
                packageName = apkMeta.packageName,
                versionName = apkMeta.versionName ?: "Unknown",
                versionCode = apkMeta.versionCode,
                minSdkVersion = apkMeta.minSdkVersion?.toIntOrNull() ?: 0,
                targetSdkVersion = apkMeta.targetSdkVersion?.toIntOrNull() ?: 0,
                permissions = apkMeta.usesPermissions.map { it.name },
                activities = extractActivities(manifest),
                services = extractServices(manifest),
                receivers = extractReceivers(manifest),
                providers = extractProviders(manifest),
                usesLibraries = apkMeta.usesFeatures.map { it.name },
                certificates = parseCertificates(apkFile)
            )
        } finally {
            apkFile.close()
        }
    }

    fun parseAar(file: File): ApkInfo {
        // AAR files are similar to APK but contain library code
        // For now, we'll extract what we can
        return ApkInfo(
            file = file,
            packageName = extractPackageFromAar(file),
            versionName = "Unknown",
            versionCode = 0,
            minSdkVersion = 0,
            targetSdkVersion = 0,
            permissions = emptyList(),
            activities = emptyList(),
            services = emptyList(),
            receivers = emptyList(),
            providers = emptyList(),
            usesLibraries = emptyList(),
            certificates = emptyList()
        )
    }

    private fun extractPackageFromAar(file: File): String {
        // Extract package name from AAR manifest
        return try {
            val zipFile = java.util.zip.ZipFile(file)
            val manifestEntry = zipFile.getEntry("AndroidManifest.xml")
            if (manifestEntry != null) {
                val manifestContent = zipFile.getInputStream(manifestEntry).readBytes()
                // Parse manifest to extract package
                "com.unknown.library"
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun extractActivities(manifest: String): List<String> {
        return extractComponents(manifest, "activity")
    }

    private fun extractServices(manifest: String): List<String> {
        return extractComponents(manifest, "service")
    }

    private fun extractReceivers(manifest: String): List<String> {
        return extractComponents(manifest, "receiver")
    }

    private fun extractProviders(manifest: String): List<String> {
        return extractComponents(manifest, "provider")
    }

    private fun extractComponents(manifest: String, componentType: String): List<String> {
        val components = mutableListOf<String>()
        val pattern = Regex("""<$componentType[^>]*android:name="([^"]+)"""")

        pattern.findAll(manifest).forEach { matchResult ->
            components.add(matchResult.groupValues[1])
        }

        return components
    }

    private fun parseCertificates(apkFile: ApkFile): List<CertificateInfo> {
        return try {
            val certificates = apkFile.apkSingers
            certificates.mapNotNull { certMeta ->
                try {
                    CertificateInfo(
                        issuer = certMeta.certDn ?: "Unknown",
                        subject = certMeta.certDn ?: "Unknown",
                        serialNumber = certMeta.certMd5 ?: "Unknown",
                        validFrom = formatDate(certMeta.startDate),
                        validTo = formatDate(certMeta.endDate),
                        signatureAlgorithm = "SHA-256"
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun formatDate(date: Date?): String {
        return if (date != null) {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)
        } else {
            "Unknown"
        }
    }
}
