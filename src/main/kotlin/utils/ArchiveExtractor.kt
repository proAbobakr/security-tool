package utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Utility class for extracting archive files (APK, AAR, AAB, JAR)
 * All these formats are ZIP-based archives
 */
class ArchiveExtractor {

    data class ExtractionResult(
        val success: Boolean,
        val extractedPath: String? = null,
        val filesExtracted: Int = 0,
        val errorMessage: String? = null
    )

    /**
     * Extract an archive file to a destination directory
     * @param archiveFile The archive file to extract (APK, AAR, AAB, or JAR)
     * @param destinationDir The directory where files will be extracted
     * @param onProgress Optional callback for progress updates
     * @return ExtractionResult containing the result information
     */
    fun extractArchive(
        archiveFile: File,
        destinationDir: File,
        onProgress: ((String) -> Unit)? = null
    ): ExtractionResult {
        try {
            // Validate input
            if (!archiveFile.exists()) {
                return ExtractionResult(
                    success = false,
                    errorMessage = "Archive file does not exist: ${archiveFile.absolutePath}"
                )
            }

            // Check if the path is a directory instead of a file
            if (archiveFile.isDirectory) {
                return ExtractionResult(
                    success = false,
                    errorMessage = "Selected path is a directory, not a file. Please select an archive file (APK, AAR, AAB, or JAR)."
                )
            }

            if (!isValidArchive(archiveFile)) {
                return ExtractionResult(
                    success = false,
                    errorMessage = "Not a valid archive file. Supported formats: APK, AAR, AAB, JAR"
                )
            }

            // Create destination directory if it doesn't exist
            if (!destinationDir.exists()) {
                destinationDir.mkdirs()
            }

            var filesExtracted = 0

            // Extract the ZIP archive
            ZipInputStream(FileInputStream(archiveFile)).use { zipInputStream ->
                var entry: ZipEntry? = zipInputStream.nextEntry

                while (entry != null) {
                    val entryName = entry.name
                    onProgress?.invoke("Extracting: $entryName")

                    val destFile = File(destinationDir, entryName)

                    // Prevent zip-slip vulnerability
                    if (!destFile.canonicalPath.startsWith(destinationDir.canonicalPath)) {
                        return ExtractionResult(
                            success = false,
                            errorMessage = "Potential zip-slip attack detected in entry: $entryName"
                        )
                    }

                    if (entry.isDirectory) {
                        destFile.mkdirs()
                    } else {
                        // Create parent directories if needed
                        destFile.parentFile?.mkdirs()

                        // Extract file
                        FileOutputStream(destFile).use { outputStream ->
                            zipInputStream.copyTo(outputStream)
                        }
                        filesExtracted++
                    }

                    zipInputStream.closeEntry()
                    entry = zipInputStream.nextEntry
                }
            }

            onProgress?.invoke("Extraction complete!")

            return ExtractionResult(
                success = true,
                extractedPath = destinationDir.absolutePath,
                filesExtracted = filesExtracted
            )

        } catch (e: Exception) {
            return ExtractionResult(
                success = false,
                errorMessage = "Error extracting archive: ${e.message}"
            )
        }
    }

    /**
     * Check if the file is a valid archive (APK, AAR, AAB, or JAR)
     */
    private fun isValidArchive(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension in listOf("apk", "aar", "aab", "jar")
    }

    /**
     * Get information about an archive without extracting it
     */
    fun getArchiveInfo(archiveFile: File): ArchiveInfo? {
        try {
            if (!archiveFile.exists() || archiveFile.isDirectory || !isValidArchive(archiveFile)) {
                return null
            }

            var entryCount = 0
            val entries = mutableListOf<String>()

            ZipInputStream(FileInputStream(archiveFile)).use { zipInputStream ->
                var entry: ZipEntry? = zipInputStream.nextEntry

                while (entry != null) {
                    entryCount++
                    if (entries.size < 100) { // Limit preview to first 100 entries
                        entries.add(entry.name)
                    }
                    zipInputStream.closeEntry()
                    entry = zipInputStream.nextEntry
                }
            }

            return ArchiveInfo(
                fileName = archiveFile.name,
                fileSize = archiveFile.length(),
                entryCount = entryCount,
                previewEntries = entries
            )

        } catch (e: Exception) {
            return null
        }
    }

    data class ArchiveInfo(
        val fileName: String,
        val fileSize: Long,
        val entryCount: Int,
        val previewEntries: List<String>
    )
}
