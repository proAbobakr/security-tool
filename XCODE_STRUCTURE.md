# Xcode Project Structure

## Overview

This document describes the proposed Xcode project structure for the **Security Tool** - a macOS application designed to analyze Android artifacts (AAR, JAR, and APK files) for security vulnerabilities and insights.

## Project Type

- **Platform**: macOS
- **Language**: Swift 5.9+
- **UI Framework**: SwiftUI
- **Minimum Deployment Target**: macOS 13.0
- **Project Format**: Xcode Project (`.xcodeproj`)

---

## Xcode Project Organization

### 1. Root Level Structure

```
security-tool/
├── SecurityTool.xcodeproj/          # Xcode project file
│   ├── project.pbxproj              # Project configuration
│   └── xcshareddata/                # Shared schemes
├── SecurityTool/                    # Main application target
├── SecurityToolTests/               # Unit tests
├── SecurityToolUITests/             # UI tests
├── Frameworks/                      # Third-party frameworks
├── Resources/                       # Shared resources
└── README.md                        # Project documentation
```

---

## 2. Main Application Target: `SecurityTool/`

### Directory Structure

```
SecurityTool/
├── App/
│   ├── SecurityToolApp.swift        # App entry point (@main)
│   ├── AppDelegate.swift            # App lifecycle management
│   └── Info.plist                   # App configuration
│
├── Core/
│   ├── Analysis/                    # Core analysis engines
│   │   ├── AARAnalyzer.swift        # Android Archive analyzer
│   │   ├── JARAnalyzer.swift        # Java Archive analyzer
│   │   ├── APKAnalyzer.swift        # Android Package analyzer
│   │   └── AnalysisEngine.swift    # Main analysis coordinator
│   │
│   ├── Parser/                      # File parsing logic
│   │   ├── ZipParser.swift          # ZIP file extraction
│   │   ├── ManifestParser.swift    # Android manifest parser
│   │   ├── DEXParser.swift          # DEX bytecode parser
│   │   └── ClassParser.swift       # Java class file parser
│   │
│   └── Security/                    # Security checks
│       ├── PermissionAnalyzer.swift # Permission analysis
│       ├── VulnerabilityScanner.swift # Vulnerability detection
│       ├── CryptoAnalyzer.swift     # Cryptographic usage check
│       └── NetworkSecurityCheck.swift # Network security analysis
│
├── Models/
│   ├── AnalysisResult.swift         # Analysis result data model
│   ├── SecurityIssue.swift          # Security issue representation
│   ├── FileMetadata.swift           # File metadata structure
│   ├── Permission.swift             # Android permission model
│   └── Vulnerability.swift          # Vulnerability details
│
├── Views/
│   ├── Main/
│   │   ├── MainView.swift           # Main application window
│   │   ├── SidebarView.swift       # Sidebar navigation
│   │   └── ContentView.swift       # Content area
│   │
│   ├── Analysis/
│   │   ├── FileDropView.swift      # Drag & drop interface
│   │   ├── AnalysisProgressView.swift # Progress indicator
│   │   └── ResultsView.swift       # Analysis results display
│   │
│   ├── Details/
│   │   ├── PermissionsView.swift   # Permissions list view
│   │   ├── VulnerabilitiesView.swift # Vulnerabilities list
│   │   ├── ComponentsView.swift    # App components view
│   │   └── MetadataView.swift      # File metadata view
│   │
│   └── Components/
│       ├── IssueRowView.swift      # Security issue row
│       ├── SeverityBadge.swift     # Severity indicator
│       └── ExportButton.swift      # Export functionality
│
├── ViewModels/
│   ├── AnalysisViewModel.swift      # Analysis state management
│   ├── FileViewModel.swift          # File handling logic
│   └── ResultsViewModel.swift      # Results presentation logic
│
├── Services/
│   ├── FileService.swift            # File I/O operations
│   ├── ReportGenerator.swift       # Report generation (PDF, JSON)
│   ├── CacheService.swift          # Analysis caching
│   └── ExportService.swift         # Export functionality
│
├── Utilities/
│   ├── Extensions/
│   │   ├── String+Extensions.swift  # String utilities
│   │   ├── Data+Extensions.swift    # Data manipulation
│   │   └── URL+Extensions.swift     # URL helpers
│   │
│   ├── Helpers/
│   │   ├── Logger.swift             # Logging utility
│   │   ├── Constants.swift          # App constants
│   │   └── ErrorHandler.swift      # Error handling
│   │
│   └── Protocols/
│       ├── Analyzable.swift         # Analysis protocol
│       └── Exportable.swift         # Export protocol
│
└── Resources/
    ├── Assets.xcassets/             # Images, icons, colors
    │   ├── AppIcon.appiconset/     # App icon
    │   ├── Colors/                  # Color assets
    │   └── Icons/                   # UI icons
    │
    ├── Localizable.xcstrings        # Localization strings
    └── Entitlements.plist           # App entitlements
```

---

## 3. Test Targets

### SecurityToolTests/

```
SecurityToolTests/
├── Core/
│   ├── AARAnalyzerTests.swift
│   ├── JARAnalyzerTests.swift
│   └── APKAnalyzerTests.swift
│
├── Parser/
│   ├── ZipParserTests.swift
│   └── ManifestParserTests.swift
│
├── Services/
│   └── FileServiceTests.swift
│
├── Mocks/
│   ├── MockFileService.swift
│   └── MockAnalyzer.swift
│
└── TestResources/
    ├── sample.aar
    ├── sample.jar
    └── sample.apk
```

### SecurityToolUITests/

```
SecurityToolUITests/
├── MainViewUITests.swift
├── AnalysisFlowUITests.swift
└── ExportUITests.swift
```

---

## 4. Xcode Target Configuration

### Main App Target: `SecurityTool`

**Product Name**: Security Tool
**Bundle Identifier**: com.yourcompany.securitytool
**Product Type**: Application
**Deployment Target**: macOS 13.0

**Capabilities**:
- Hardened Runtime
- App Sandbox (with file access permissions)
- Code signing

**Build Settings**:
- Swift Language Version: 5.9
- Optimization Level: `-O` (Release), `-Onone` (Debug)
- Enable Testability: YES (Debug only)

**App Entitlements**:
```xml
com.apple.security.app-sandbox = YES
com.apple.security.files.user-selected.read-only = YES
com.apple.security.files.downloads.read-only = YES
com.apple.security.temporary-exception.files.absolute-path.read-only = [/tmp/]
```

---

## 5. Dependencies & Frameworks

### External Dependencies (via Swift Package Manager)

```
SecurityTool.xcodeproj/project.pbxproj
├── Package Dependencies:
    ├── ZIPFoundation                # ZIP file handling
    │   └── URL: https://github.com/weichsel/ZIPFoundation.git
    │
    ├── SwiftSyntax                  # Code analysis
    │   └── URL: https://github.com/apple/swift-syntax.git
    │
    └── CryptoSwift                  # Cryptographic utilities
        └── URL: https://github.com/krzyzanowskim/CryptoSwift.git
```

### System Frameworks

- **Foundation** - Core Swift/Cocoa functionality
- **SwiftUI** - User interface
- **UniformTypeIdentifiers** - File type handling
- **Security** - Cryptographic operations
- **OSLog** - System logging

---

## 6. Build Schemes

### SecurityTool (Development)

- **Build Configuration**: Debug
- **Run Destination**: My Mac
- **Executable**: SecurityTool.app
- **Test Plans**: SecurityToolTests

### SecurityTool (Release)

- **Build Configuration**: Release
- **Optimization**: Enabled
- **Code Signing**: Distribution certificate
- **Notarization**: Enabled

---

## 7. Xcode Groups vs File System

Xcode uses **groups** to organize files visually, which may differ from the actual file system structure.

**Recommended Approach**: Mirror Xcode groups to file system folders for consistency.

```
Xcode Navigator (Groups)          File System
├── 📁 SecurityTool               ├── SecurityTool/
│   ├── 📁 App                    │   ├── App/
│   ├── 📁 Core                   │   ├── Core/
│   ├── 📁 Models                 │   ├── Models/
│   ├── 📁 Views                  │   ├── Views/
│   ├── 📁 ViewModels             │   ├── ViewModels/
│   ├── 📁 Services               │   ├── Services/
│   ├── 📁 Utilities              │   ├── Utilities/
│   └── 📁 Resources              │   └── Resources/
```

---

## 8. Key Xcode Files Explained

### `project.pbxproj`

The main project file containing:
- Build configurations (Debug, Release)
- Target settings
- File references
- Build phases
- Dependencies

### `Info.plist`

Application metadata:
```xml
<key>CFBundleName</key>
<string>Security Tool</string>

<key>CFBundleIdentifier</key>
<string>$(PRODUCT_BUNDLE_IDENTIFIER)</string>

<key>LSMinimumSystemVersion</key>
<string>13.0</string>

<key>NSHumanReadableCopyright</key>
<string>Copyright © 2025. All rights reserved.</string>

<key>UTImportedTypeDeclarations</key>
<array>
    <!-- AAR, JAR, APK file type support -->
</array>
```

### `Entitlements.plist`

Security permissions required by the app:
- File access permissions
- Network access
- Hardened runtime exceptions

---

## 9. Build Phases

### Target Build Phases Order:

1. **Dependencies** - Link dependencies
2. **Compile Sources** - Compile Swift files (`.swift` files)
3. **Link Binary With Libraries** - Link frameworks
4. **Copy Bundle Resources** - Copy assets, xibs, storyboards
5. **Embed Frameworks** - Embed external frameworks
6. **Run Script** - Custom build scripts (e.g., SwiftLint)

---

## 10. Asset Catalog Structure

### `Assets.xcassets/`

```
Assets.xcassets/
├── AppIcon.appiconset/
│   ├── Contents.json
│   ├── icon_16x16.png
│   ├── icon_32x32.png
│   ├── icon_128x128.png
│   ├── icon_256x256.png
│   ├── icon_512x512.png
│   └── icon_1024x1024.png
│
├── Colors/
│   ├── PrimaryColor.colorset/
│   ├── SuccessColor.colorset/
│   ├── WarningColor.colorset/
│   └── DangerColor.colorset/
│
└── Icons/
    ├── FileIcon.imageset/
    ├── AnalysisIcon.imageset/
    └── ExportIcon.imageset/
```

---

## 11. Workspace vs Project

### Current Structure: `.xcodeproj`

Single project with multiple targets.

### Future Consideration: `.xcworkspace`

If the project grows to include:
- Multiple projects
- CLI tool target
- Framework modules

Consider migrating to a workspace:
```
SecurityTool.xcworkspace/
├── SecurityTool.xcodeproj
├── SecurityToolCore.xcodeproj     # Core framework
└── SecurityToolCLI.xcodeproj      # Command-line tool
```

---

## 12. Code Organization Best Practices

### File Naming Conventions

- **Swift Files**: PascalCase (e.g., `AARAnalyzer.swift`)
- **Resource Files**: kebab-case (e.g., `app-icon.png`)
- **Test Files**: `[FileName]Tests.swift`

### Group Organization

- Group by **feature** (e.g., Analysis, Export)
- Keep **Models** separate and reusable
- Place **Utilities** in a dedicated folder
- Use **nested groups** for large features

### Target Membership

- Main app files → `SecurityTool` target
- Test files → `SecurityToolTests` target
- Shared code → Multiple targets (if needed)

---

## 13. Development Workflow in Xcode

### Adding New Features

1. Create feature group in Xcode Navigator
2. Add Swift files for models, views, logic
3. Update ViewModels if needed
4. Write unit tests
5. Add UI tests for user-facing features

### File Template Usage

Use Xcode templates for consistency:
- **SwiftUI View** template for views
- **Swift File** template for models/services
- **Unit Test Case Class** for tests

---

## 14. Version Control Integration

### Git Integration in Xcode

Xcode integrates with Git for:
- Source control management
- Branch operations
- Commit and push
- Merge conflict resolution

### `.gitignore` Recommendations

```gitignore
# Xcode
*.xcodeproj/*
!*.xcodeproj/project.pbxproj
!*.xcodeproj/xcshareddata/
*.xcworkspace/*
!*.xcworkspace/contents.xcworkspacedata

# Build
build/
DerivedData/

# User-specific
*.pbxuser
*.mode1v3
*.mode2v3
*.perspectivev3
xcuserdata/

# Provisioning
*.mobileprovision
*.cer
*.p12
```

---

## 15. Distribution Setup

### Archive Configuration

1. **Product** → **Archive**
2. Organizer opens with archived builds
3. Select archive → **Distribute App**
4. Choose distribution method:
   - Mac App Store
   - Developer ID (Notarized)
   - Development

### Notarization

Required for macOS apps distributed outside App Store:
```bash
xcrun notarytool submit SecurityTool.app --wait
```

---

## Summary

This Xcode project structure provides a solid foundation for developing the Security Tool as a professional macOS application. The organization emphasizes:

- **Modularity**: Separate concerns (Core, Views, Services)
- **Testability**: Dedicated test targets with mocks
- **Scalability**: Easy to add new analyzers and features
- **Maintainability**: Clear file organization and naming

Next steps would involve creating the actual `.xcodeproj` file and implementing the core analysis engines for AAR, JAR, and APK files.
