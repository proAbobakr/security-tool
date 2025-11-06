import SwiftUI
import UniformTypeIdentifiers

struct ArchiveImporterView: View {
    @State private var selectedFile: URL?
    @State private var archiveInfo: ArchiveExtractor.ArchiveInfo?
    @State private var extractionResult: ArchiveExtractor.ExtractionResult?
    @State private var progressMessages: [String] = []
    @State private var isExtracting = false
    @State private var isFileImporterPresented = false

    private let extractor = ArchiveExtractor()

    var body: some View {
        ZStack {
            // Dark background
            Color(red: 0.12, green: 0.12, blue: 0.12)
                .ignoresSafeArea()

            ScrollView {
                VStack(alignment: .center, spacing: 24) {
                    // Title
                    VStack(spacing: 8) {
                        Text("Archive Importer & Analyzer")
                            .font(.system(size: 28, weight: .bold))
                            .foregroundColor(.white)

                        Text("Import and extract APK, AAR, AAB, and JAR files")
                            .font(.system(size: 14))
                            .foregroundColor(.gray)
                    }
                    .padding(.top, 24)

                    // Select File Button
                    Button(action: {
                        isFileImporterPresented = true
                    }) {
                        Text("Select Archive File")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(.white)
                            .frame(width: 300, height: 50)
                            .background(Color.blue)
                            .cornerRadius(8)
                    }
                    .fileImporter(
                        isPresented: $isFileImporterPresented,
                        allowedContentTypes: [
                            UTType(filenameExtension: "apk") ?? .data,
                            UTType(filenameExtension: "aar") ?? .data,
                            UTType(filenameExtension: "aab") ?? .data,
                            UTType(filenameExtension: "jar") ?? .data
                        ],
                        allowsMultipleSelection: false
                    ) { result in
                        handleFileSelection(result)
                    }

                    // Display selected file info
                    if let file = selectedFile {
                        selectedFileCard(file: file)

                        // Extract Button
                        Button(action: extractArchive) {
                            Text(isExtracting ? "Extracting..." : "Extract Archive")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(.white)
                                .frame(width: 300, height: 50)
                                .background(Color.green)
                                .cornerRadius(8)
                        }
                        .disabled(isExtracting)
                        .padding(.vertical, 8)
                    }

                    // Extraction Result
                    if let result = extractionResult {
                        extractionResultCard(result: result)
                    }

                    // Progress Messages
                    if !progressMessages.isEmpty {
                        progressLogCard()
                    }

                    Spacer(minLength: 24)
                }
                .frame(maxWidth: .infinity)
                .padding(.horizontal, 24)
            }
        }
    }

    // MARK: - Subviews

    @ViewBuilder
    private func selectedFileCard(file: URL) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Selected File")
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)

            Text("Name: \(file.lastPathComponent)")
                .font(.system(size: 14))
                .foregroundColor(.gray)

            if let fileSize = getFileSize(url: file) {
                Text("Size: \(formatFileSize(bytes: fileSize))")
                    .font(.system(size: 14))
                    .foregroundColor(.gray)
            }

            Text("Path: \(file.path)")
                .font(.system(size: 14))
                .foregroundColor(.gray)
                .lineLimit(2)

            if let info = archiveInfo {
                Text("Entries in archive: \(info.entryCount)")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(.green)
                    .padding(.top, 4)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Color(red: 0.18, green: 0.18, blue: 0.18))
        .cornerRadius(8)
        .shadow(radius: 4)
    }

    @ViewBuilder
    private func extractionResultCard(result: ArchiveExtractor.ExtractionResult) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(result.success ? "✓ Extraction Successful" : "✗ Extraction Failed")
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)

            if result.success {
                Text("Files extracted: \(result.filesExtracted)")
                    .font(.system(size: 14))
                    .foregroundColor(.white)

                Text("Output directory:")
                    .font(.system(size: 14))
                    .foregroundColor(.white)

                if let path = result.extractedPath {
                    Text(path)
                        .font(.system(size: 12))
                        .foregroundColor(Color(red: 0.5, green: 0.78, blue: 0.52))
                }
            } else {
                Text(result.errorMessage ?? "Unknown error")
                    .font(.system(size: 14))
                    .foregroundColor(.white)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(result.success ? Color(red: 0.11, green: 0.37, blue: 0.13) : Color(red: 0.55, green: 0, blue: 0))
        .cornerRadius(8)
        .shadow(radius: 4)
    }

    @ViewBuilder
    private func progressLogCard() -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Progress Log")
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(.white)

            ScrollView {
                VStack(alignment: .leading, spacing: 4) {
                    ForEach(progressMessages.suffix(50), id: \.self) { message in
                        HStack(alignment: .top, spacing: 4) {
                            Text("•")
                                .foregroundColor(Color(red: 0.69, green: 0.75, blue: 0.77))
                            Text(message)
                                .font(.system(size: 12))
                                .foregroundColor(Color(red: 0.69, green: 0.75, blue: 0.77))
                        }
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .frame(maxHeight: 300)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Color(red: 0.18, green: 0.18, blue: 0.18))
        .cornerRadius(8)
        .shadow(radius: 4)
    }

    // MARK: - Helper Methods

    private func handleFileSelection(_ result: Result<[URL], Error>) {
        switch result {
        case .success(let urls):
            guard let url = urls.first else { return }

            // Start accessing security-scoped resource
            guard url.startAccessingSecurityScopedResource() else {
                return
            }
            defer { url.stopAccessingSecurityScopedResource() }

            selectedFile = url
            archiveInfo = extractor.getArchiveInfo(from: url)
            extractionResult = nil
            progressMessages = []

        case .failure(let error):
            print("Error selecting file: \(error)")
        }
    }

    private func extractArchive() {
        guard let file = selectedFile else { return }

        isExtracting = true
        progressMessages = ["Starting extraction..."]

        let fileNameWithoutExt = file.deletingPathExtension().lastPathComponent
        let outputDir = file.deletingLastPathComponent()
            .appendingPathComponent("\(fileNameWithoutExt)_extracted")

        DispatchQueue.global(qos: .userInitiated).async {
            // Start accessing security-scoped resource
            guard file.startAccessingSecurityScopedResource() else {
                DispatchQueue.main.async {
                    isExtracting = false
                }
                return
            }
            defer { file.stopAccessingSecurityScopedResource() }

            let result = extractor.extractArchive(from: file, to: outputDir) { message in
                DispatchQueue.main.async {
                    progressMessages.append(message)
                }
            }

            DispatchQueue.main.async {
                extractionResult = result
                isExtracting = false
            }
        }
    }

    private func getFileSize(url: URL) -> Int64? {
        do {
            let attributes = try FileManager.default.attributesOfItem(atPath: url.path)
            return attributes[.size] as? Int64
        } catch {
            return nil
        }
    }

    private func formatFileSize(bytes: Int64) -> String {
        switch bytes {
        case 0..<1024:
            return "\(bytes) B"
        case 1024..<(1024 * 1024):
            return "\(bytes / 1024) KB"
        case (1024 * 1024)..<(1024 * 1024 * 1024):
            return "\(bytes / (1024 * 1024)) MB"
        default:
            return "\(bytes / (1024 * 1024 * 1024)) GB"
        }
    }
}

#Preview {
    ArchiveImporterView()
}
