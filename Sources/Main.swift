import SwiftUI

@main
struct SecurityToolApp: App {
    var body: some Scene {
        WindowGroup {
            ArchiveImporterView()
                .frame(minWidth: 900, minHeight: 800)
        }
        .windowStyle(.hiddenTitleBar)
        .commands {
            CommandGroup(replacing: .appInfo) {
                Button("About Security Tool") {
                    // About dialog
                }
            }
        }
    }
}

/// Entry point for the Security Tool application
/// This creates a SwiftUI macOS application with a window containing the ArchiveImporterView
///
/// Configuration:
/// - Window size: 900x800 minimum
/// - Uses hidden title bar for modern look
/// - Custom app commands
