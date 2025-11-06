import Foundation
import ZIPFoundation

/// Utility class for extracting archive files (APK, AAR, AAB, JAR)
/// All these formats are ZIP-based archives
class ArchiveExtractor {

    // MARK: - Types

    struct ExtractionResult {
        let success: Bool
        let extractedPath: String?
        let filesExtracted: Int
        let errorMessage: String?

        static func success(path: String, fileCount: Int) -> ExtractionResult {
            ExtractionResult(success: true, extractedPath: path, filesExtracted: fileCount, errorMessage: nil)
        }

        static func failure(message: String) -> ExtractionResult {
            ExtractionResult(success: false, extractedPath: nil, filesExtracted: 0, errorMessage: message)
        }
    }

    struct ArchiveInfo {
        let fileName: String
        let fileSize: Int64
        let entryCount: Int
        let previewEntries: [String]
    }

    // MARK: - Public Methods

    /// Extract an archive file to a destination directory
    /// - Parameters:
    ///   - archiveURL: The archive file to extract (APK, AAR, AAB, or JAR)
    ///   - destinationURL: The directory where files will be extracted
    ///   - onProgress: Optional callback for progress updates
    /// - Returns: ExtractionResult containing the result information
    func extractArchive(
        from archiveURL: URL,
        to destinationURL: URL,
        onProgress: ((String) -> Void)? = nil
    ) -> ExtractionResult {
        // Validate input
        guard FileManager.default.fileExists(atPath: archiveURL.path) else {
            return .failure(message: "Archive file does not exist: \(archiveURL.path)")
        }

        guard isValidArchive(archiveURL) else {
            return .failure(message: "Not a valid archive file. Supported formats: APK, AAR, AAB, JAR")
        }

        // Create destination directory if it doesn't exist
        do {
            try FileManager.default.createDirectory(
                at: destinationURL,
                withIntermediateDirectories: true,
                attributes: nil
            )
        } catch {
            return .failure(message: "Failed to create destination directory: \(error.localizedDescription)")
        }

        // Extract the ZIP archive
        var filesExtracted = 0

        do {
            guard let archive = Archive(url: archiveURL, accessMode: .read) else {
                return .failure(message: "Failed to open archive file")
            }

            for entry in archive {
                let entryName = entry.path
                onProgress?("Extracting: \(entryName)")

                let destURL = destinationURL.appendingPathComponent(entryName)

                // Prevent zip-slip vulnerability
                guard destURL.path.hasPrefix(destinationURL.path) else {
                    return .failure(message: "Potential zip-slip attack detected in entry: \(entryName)")
                }

                if entry.type == .directory {
                    try FileManager.default.createDirectory(
                        at: destURL,
                        withIntermediateDirectories: true,
                        attributes: nil
                    )
                } else {
                    // Create parent directories if needed
                    let parentURL = destURL.deletingLastPathComponent()
                    try FileManager.default.createDirectory(
                        at: parentURL,
                        withIntermediateDirectories: true,
                        attributes: nil
                    )

                    // Extract file
                    _ = try archive.extract(entry, to: destURL)
                    filesExtracted += 1
                }
            }

            onProgress?("Extraction complete!")

            return .success(path: destinationURL.path, fileCount: filesExtracted)

        } catch {
            return .failure(message: "Error extracting archive: \(error.localizedDescription)")
        }
    }

    /// Get information about an archive without extracting it
    /// - Parameter archiveURL: The archive file to inspect
    /// - Returns: ArchiveInfo or nil if the file cannot be read
    func getArchiveInfo(from archiveURL: URL) -> ArchiveInfo? {
        guard FileManager.default.fileExists(atPath: archiveURL.path),
              isValidArchive(archiveURL) else {
            return nil
        }

        do {
            guard let archive = Archive(url: archiveURL, accessMode: .read) else {
                return nil
            }

            var entryCount = 0
            var entries: [String] = []

            for entry in archive {
                entryCount += 1
                if entries.count < 100 { // Limit preview to first 100 entries
                    entries.append(entry.path)
                }
            }

            let attributes = try FileManager.default.attributesOfItem(atPath: archiveURL.path)
            let fileSize = attributes[.size] as? Int64 ?? 0

            return ArchiveInfo(
                fileName: archiveURL.lastPathComponent,
                fileSize: fileSize,
                entryCount: entryCount,
                previewEntries: entries
            )

        } catch {
            return nil
        }
    }

    // MARK: - Private Methods

    /// Check if the file is a valid archive (APK, AAR, AAB, or JAR)
    private func isValidArchive(_ url: URL) -> Bool {
        let validExtensions = ["apk", "aar", "aab", "jar"]
        return validExtensions.contains(url.pathExtension.lowercased())
    }
}
