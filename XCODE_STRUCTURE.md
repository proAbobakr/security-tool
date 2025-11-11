# 🏗️ Xcode Project Structure

> **Security Tool for macOS** - A comprehensive guide to the proposed Xcode project architecture for analyzing Android artifacts (AAR, JAR, and APK files) for security vulnerabilities and insights.

---

## 📑 Table of Contents

1. [Project Overview](#-project-overview)
2. [Root Level Structure](#1-root-level-structure)
3. [Main Application Target](#2-main-application-target-securitytool)
4. [Test Targets](#3-test-targets)
5. [Xcode Target Configuration](#4-xcode-target-configuration)
6. [Dependencies & Frameworks](#5-dependencies--frameworks)
7. [Build Schemes](#6-build-schemes)
8. [Xcode Groups vs File System](#7-xcode-groups-vs-file-system)
9. [Key Xcode Files](#8-key-xcode-files-explained)
10. [Build Phases](#9-build-phases)
11. [Asset Catalog Structure](#10-asset-catalog-structure)
12. [Workspace vs Project](#11-workspace-vs-project)
13. [Code Organization Best Practices](#12-code-organization-best-practices)
14. [Development Workflow](#13-development-workflow-in-xcode)
15. [Version Control Integration](#14-version-control-integration)
16. [Distribution Setup](#15-distribution-setup)

---

## 🎯 Project Overview

### Technical Specifications

| Specification | Details |
|--------------|---------|
| **Platform** | macOS |
| **Language** | Swift 5.9+ |
| **UI Framework** | SwiftUI |
| **Minimum Target** | macOS 13.0 (Ventura) |
| **Project Format** | `.xcodeproj` |
| **Architecture** | MVVM (Model-View-ViewModel) |

### Supported File Types

```
✓ AAR (Android Archive)
✓ JAR (Java Archive)
✓ APK (Android Package)
```

---

## 📦 Xcode Project Organization

### 1. Root Level Structure

```
security-tool/
│
├── 📱 SecurityTool.xcodeproj/       # Xcode project file
│   ├── project.pbxproj              # Project configuration
│   └── xcshareddata/                # Shared schemes
│
├── 📂 SecurityTool/                 # Main application target
├── 🧪 SecurityToolTests/            # Unit tests
├── 🔍 SecurityToolUITests/          # UI tests
├── 📚 Frameworks/                   # Third-party frameworks
├── 🎨 Resources/                    # Shared resources
└── 📄 README.md                     # Project documentation
```

| Directory | Purpose | Type |
|-----------|---------|------|
| `SecurityTool.xcodeproj/` | Xcode project file and configuration | Project |
| `SecurityTool/` | Main application source code | Target |
| `SecurityToolTests/` | Unit and integration tests | Test Target |
| `SecurityToolUITests/` | User interface tests | Test Target |
| `Frameworks/` | External dependencies | Resources |
| `Resources/` | Shared assets and files | Resources |

---

## 2. Main Application Target: `SecurityTool/`

### 📂 Directory Structure Overview

The main application follows a clean architecture pattern with clear separation of concerns:

```
SecurityTool/
│
├── 🚀 App/                          # Application Entry Point
│   ├── SecurityToolApp.swift        # @main - App entry
│   ├── AppDelegate.swift            # Lifecycle management
│   └── Info.plist                   # App configuration
│
├── 🔧 Core/                         # Core Business Logic
│   │
│   ├── 📊 Analysis/                 # Analysis Engines
│   │   ├── AARAnalyzer.swift        # Android Archive
│   │   ├── JARAnalyzer.swift        # Java Archive
│   │   ├── APKAnalyzer.swift        # Android Package
│   │   └── AnalysisEngine.swift     # Main coordinator
│   │
│   ├── 📄 Parser/                   # File Parsers
│   │   ├── ZipParser.swift          # ZIP extraction
│   │   ├── ManifestParser.swift     # Android manifest
│   │   ├── DEXParser.swift          # DEX bytecode
│   │   └── ClassParser.swift        # Java class files
│   │
│   └── 🔒 Security/                 # Security Analysis
│       ├── PermissionAnalyzer.swift # Permissions
│       ├── VulnerabilityScanner.swift # Vulnerabilities
│       ├── CryptoAnalyzer.swift     # Cryptography
│       └── NetworkSecurityCheck.swift # Network security
│
├── 📦 Models/                       # Data Models
│   ├── AnalysisResult.swift         # Analysis results
│   ├── SecurityIssue.swift          # Security issues
│   ├── FileMetadata.swift           # File metadata
│   ├── Permission.swift             # Permissions
│   └── Vulnerability.swift          # Vulnerabilities
│
├── 🎨 Views/                        # User Interface (SwiftUI)
│   │
│   ├── Main/                        # Main Views
│   │   ├── MainView.swift           # Root window
│   │   ├── SidebarView.swift       # Navigation
│   │   └── ContentView.swift       # Content area
│   │
│   ├── Analysis/                    # Analysis Interface
│   │   ├── FileDropView.swift      # Drag & drop
│   │   ├── AnalysisProgressView.swift # Progress
│   │   └── ResultsView.swift       # Results display
│   │
│   ├── Details/                     # Detail Views
│   │   ├── PermissionsView.swift   # Permissions list
│   │   ├── VulnerabilitiesView.swift # Vulnerabilities
│   │   ├── ComponentsView.swift    # Components
│   │   └── MetadataView.swift      # Metadata
│   │
│   └── Components/                  # Reusable Components
│       ├── IssueRowView.swift      # Issue row
│       ├── SeverityBadge.swift     # Severity badge
│       └── ExportButton.swift      # Export button
│
├── 🎯 ViewModels/                   # MVVM View Models
│   ├── AnalysisViewModel.swift      # Analysis state
│   ├── FileViewModel.swift          # File handling
│   └── ResultsViewModel.swift      # Results logic
│
├── ⚙️ Services/                     # Application Services
│   ├── FileService.swift            # File I/O
│   ├── ReportGenerator.swift       # PDF/JSON reports
│   ├── CacheService.swift          # Caching
│   └── ExportService.swift         # Export logic
│
├── 🛠️ Utilities/                    # Helper Classes
│   │
│   ├── Extensions/                  # Swift Extensions
│   │   ├── String+Extensions.swift  # String utils
│   │   ├── Data+Extensions.swift    # Data utils
│   │   └── URL+Extensions.swift     # URL utils
│   │
│   ├── Helpers/                     # Helper Classes
│   │   ├── Logger.swift             # Logging
│   │   ├── Constants.swift          # Constants
│   │   └── ErrorHandler.swift      # Error handling
│   │
│   └── Protocols/                   # Protocol Definitions
│       ├── Analyzable.swift         # Analysis protocol
│       └── Exportable.swift         # Export protocol
│
└── 🎨 Resources/                    # Application Resources
    ├── Assets.xcassets/             # Visual assets
    │   ├── AppIcon.appiconset/     # App icons
    │   ├── Colors/                  # Color palette
    │   └── Icons/                   # UI icons
    │
    ├── Localizable.xcstrings        # Translations
    └── Entitlements.plist           # Permissions
```

### 📋 Module Breakdown

| Module | Purpose | Key Components |
|--------|---------|----------------|
| **App** | Application lifecycle and entry point | `@main`, AppDelegate |
| **Core** | Business logic and analysis engines | Analyzers, Parsers, Security |
| **Models** | Data structures and entities | Results, Issues, Metadata |
| **Views** | SwiftUI user interface components | Main, Analysis, Details |
| **ViewModels** | MVVM presentation logic | State management, Data binding |
| **Services** | Reusable application services | File handling, Reporting |
| **Utilities** | Helper functions and extensions | Extensions, Protocols |
| **Resources** | Assets and configuration files | Images, Localization |

---

## 3. 🧪 Test Targets

### Unit Tests: `SecurityToolTests/`

```
SecurityToolTests/
│
├── 🔧 Core/                         # Core Logic Tests
│   ├── AARAnalyzerTests.swift       # AAR analyzer tests
│   ├── JARAnalyzerTests.swift       # JAR analyzer tests
│   └── APKAnalyzerTests.swift       # APK analyzer tests
│
├── 📄 Parser/                       # Parser Tests
│   ├── ZipParserTests.swift         # ZIP parser tests
│   └── ManifestParserTests.swift    # Manifest tests
│
├── ⚙️ Services/                     # Service Tests
│   └── FileServiceTests.swift       # File service tests
│
├── 🎭 Mocks/                        # Test Mocks
│   ├── MockFileService.swift       # Mock file service
│   └── MockAnalyzer.swift          # Mock analyzer
│
└── 📦 TestResources/                # Test Assets
    ├── sample.aar                   # Sample AAR file
    ├── sample.jar                   # Sample JAR file
    └── sample.apk                   # Sample APK file
```

### UI Tests: `SecurityToolUITests/`

```
SecurityToolUITests/
│
├── MainViewUITests.swift            # Main view tests
├── AnalysisFlowUITests.swift        # Analysis workflow tests
└── ExportUITests.swift              # Export feature tests
```

### 📊 Test Coverage Goals

| Component | Target Coverage | Priority |
|-----------|----------------|----------|
| Core/Analysis | 90%+ | High |
| Core/Parser | 85%+ | High |
| Services | 80%+ | Medium |
| ViewModels | 75%+ | Medium |
| Utilities | 70%+ | Low |

---

## 4. ⚙️ Xcode Target Configuration

### Main App Target: `SecurityTool`

#### Basic Configuration

| Setting | Value |
|---------|-------|
| **Product Name** | Security Tool |
| **Bundle Identifier** | `com.yourcompany.securitytool` |
| **Product Type** | Application |
| **Deployment Target** | macOS 13.0 (Ventura) |
| **Swift Version** | 5.9 |

#### Capabilities & Signing

```
✓ Hardened Runtime
✓ App Sandbox (with file access)
✓ Code Signing (Developer ID)
```

#### Build Configuration

| Configuration | Debug | Release |
|---------------|-------|---------|
| **Optimization** | `-Onone` | `-O` |
| **Testability** | Enabled | Disabled |
| **Swift Compilation** | Incremental | Whole Module |
| **Debug Symbols** | Yes | Yes |
| **Bitcode** | No | No |

#### App Entitlements

```xml
<!-- Sandbox & File Access -->
<key>com.apple.security.app-sandbox</key>
<true/>

<key>com.apple.security.files.user-selected.read-only</key>
<true/>

<key>com.apple.security.files.downloads.read-only</key>
<true/>

<key>com.apple.security.temporary-exception.files.absolute-path.read-only</key>
<array>
    <string>/tmp/</string>
</array>
```

> **Note**: These entitlements allow the app to read user-selected files and downloaded content while maintaining sandbox security.

---

## 5. 📚 Dependencies & Frameworks

### External Dependencies (Swift Package Manager)

| Package | Purpose | Repository | Version |
|---------|---------|-----------|---------|
| **ZIPFoundation** | ZIP file extraction & handling | [GitHub](https://github.com/weichsel/ZIPFoundation.git) | Latest |
| **SwiftSyntax** | Code analysis utilities | [GitHub](https://github.com/apple/swift-syntax.git) | Latest |
| **CryptoSwift** | Cryptographic operations | [GitHub](https://github.com/krzyzanowskim/CryptoSwift.git) | Latest |

```
📦 Package Dependencies
│
├── ZIPFoundation
│   ├── Purpose: ZIP file handling
│   └── Used by: Parser module
│
├── SwiftSyntax
│   ├── Purpose: Code analysis
│   └── Used by: Analysis module
│
└── CryptoSwift
    ├── Purpose: Cryptographic utilities
    └── Used by: Security module
```

### System Frameworks

| Framework | Purpose | Module |
|-----------|---------|--------|
| **Foundation** | Core Swift/Cocoa functionality | All |
| **SwiftUI** | User interface framework | Views |
| **UniformTypeIdentifiers** | File type identification | Core |
| **Security** | Cryptographic operations | Core/Security |
| **OSLog** | System logging | Utilities |

---

## 6. 🔨 Build Schemes

### Development Scheme

| Setting | Value |
|---------|-------|
| **Name** | SecurityTool (Development) |
| **Configuration** | Debug |
| **Run Destination** | My Mac |
| **Executable** | SecurityTool.app |
| **Test Plan** | SecurityToolTests |
| **Environment Variables** | `DEBUG=1` |

**Actions:**
```
✓ Build
✓ Run
✓ Test
✓ Profile
✓ Analyze
✓ Archive
```

### Release Scheme

| Setting | Value |
|---------|-------|
| **Name** | SecurityTool (Release) |
| **Configuration** | Release |
| **Optimization** | Enabled (`-O`) |
| **Code Signing** | Distribution Certificate |
| **Notarization** | Enabled |

**Actions:**
```
✓ Build
✓ Archive (for distribution)
```

---

## 7. 📁 Xcode Groups vs File System

### Understanding Groups

> **Important**: Xcode uses **groups** (virtual folders) to organize files, which may differ from the actual file system structure.

**Recommended Approach**: Mirror Xcode groups to file system folders for consistency and clarity.

### Side-by-Side Comparison

```
┌─────────────────────────────────┬─────────────────────────────────┐
│   Xcode Navigator (Groups)      │      File System                │
├─────────────────────────────────┼─────────────────────────────────┤
│ 📁 SecurityTool                  │ SecurityTool/                   │
│   ├── 📁 App                     │   ├── App/                      │
│   ├── 📁 Core                    │   ├── Core/                     │
│   ├── 📁 Models                  │   ├── Models/                   │
│   ├── 📁 Views                   │   ├── Views/                    │
│   ├── 📁 ViewModels              │   ├── ViewModels/               │
│   ├── 📁 Services                │   ├── Services/                 │
│   ├── 📁 Utilities               │   ├── Utilities/                │
│   └── 📁 Resources               │   └── Resources/                │
└─────────────────────────────────┴─────────────────────────────────┘
```

### Best Practices

✅ **Do:**
- Keep groups synchronized with file system
- Use consistent naming conventions
- Group files by feature/functionality

❌ **Don't:**
- Create groups without corresponding folders
- Mix unrelated files in single groups
- Use deep nesting (max 3-4 levels)

---

## 8. 📝 Key Xcode Files Explained

### `project.pbxproj`

The core project configuration file (text-based format).

**Contains:**
- Build configurations (Debug, Release)
- Target settings and dependencies
- File references and groups
- Build phases
- Swift Package dependencies

> **Note**: This file is auto-generated. Edit through Xcode UI when possible.

### `Info.plist`

Application metadata and configuration.

```xml
<!-- App Identity -->
<key>CFBundleName</key>
<string>Security Tool</string>

<key>CFBundleIdentifier</key>
<string>$(PRODUCT_BUNDLE_IDENTIFIER)</string>

<!-- System Requirements -->
<key>LSMinimumSystemVersion</key>
<string>13.0</string>

<!-- Copyright -->
<key>NSHumanReadableCopyright</key>
<string>Copyright © 2025. All rights reserved.</string>

<!-- File Type Support -->
<key>UTImportedTypeDeclarations</key>
<array>
    <!-- AAR, JAR, APK file type declarations -->
</array>
```

### `Entitlements.plist`

Sandbox permissions and capabilities.

| Entitlement | Purpose |
|-------------|---------|
| `app-sandbox` | Enable App Sandbox |
| `files.user-selected.read-only` | Read user-selected files |
| `files.downloads.read-only` | Access Downloads folder |
| `network.client` | Network access (if needed) |

---

## 9. 🔧 Build Phases

### Target Build Phases Order

The build system executes these phases sequentially:

| Phase | Description | Input | Output |
|-------|-------------|-------|--------|
| **1. Dependencies** | Resolve package dependencies | SPM packages | Resolved packages |
| **2. Compile Sources** | Compile Swift files | `.swift` files | `.o` object files |
| **3. Link Binary** | Link libraries and frameworks | Object files | Binary executable |
| **4. Copy Resources** | Copy assets to bundle | Images, xibs, etc. | App bundle |
| **5. Embed Frameworks** | Embed external frameworks | Frameworks | Embedded frameworks |
| **6. Run Script** | Custom build scripts | Scripts | Custom output |

### Build Phase Diagram

```
┌─────────────────┐
│  Dependencies   │ ← Resolve Swift packages
└────────┬────────┘
         │
┌────────▼────────┐
│ Compile Sources │ ← Compile .swift files
└────────┬────────┘
         │
┌────────▼────────┐
│  Link Binary    │ ← Link frameworks
└────────┬────────┘
         │
┌────────▼────────┐
│ Copy Resources  │ ← Copy assets
└────────┬────────┘
         │
┌────────▼────────┐
│Embed Frameworks │ ← Embed dependencies
└────────┬────────┘
         │
┌────────▼────────┐
│   Run Script    │ ← SwiftLint, etc.
└────────┬────────┘
         │
     ┌───▼───┐
     │  App  │
     └───────┘
```

### Custom Build Scripts

**Example: SwiftLint Integration**

```bash
# SwiftLint
if which swiftlint >/dev/null; then
  swiftlint
else
  echo "warning: SwiftLint not installed"
fi
```

---

## 10. 🎨 Asset Catalog Structure

### `Assets.xcassets/` Organization

```
Assets.xcassets/
│
├── 📱 AppIcon.appiconset/           # Application Icon
│   ├── Contents.json                # Icon metadata
│   ├── icon_16x16.png               # Menu bar size
│   ├── icon_32x32.png               # Retina menu bar
│   ├── icon_128x128.png             # Standard size
│   ├── icon_256x256.png             # Retina standard
│   ├── icon_512x512.png             # Large size
│   └── icon_1024x1024.png           # App Store
│
├── 🎨 Colors/                       # Color Assets
│   ├── PrimaryColor.colorset/       # Brand primary color
│   ├── SuccessColor.colorset/       # Success state (green)
│   ├── WarningColor.colorset/       # Warning state (orange)
│   └── DangerColor.colorset/        # Danger state (red)
│
└── 🖼️ Icons/                         # UI Icons
    ├── FileIcon.imageset/           # File icon
    ├── AnalysisIcon.imageset/       # Analysis icon
    └── ExportIcon.imageset/         # Export icon
```

### macOS Icon Sizes

| Size | Purpose | Required |
|------|---------|----------|
| 16×16 | Menu bar | Yes |
| 32×32 | Retina menu bar | Yes |
| 128×128 | Dock | Yes |
| 256×256 | Retina dock | Yes |
| 512×512 | Finder | Yes |
| 1024×1024 | App Store | Yes |

### Color Palette

```swift
// Usage in SwiftUI
Color("PrimaryColor")    // #007AFF (Blue)
Color("SuccessColor")    // #34C759 (Green)
Color("WarningColor")    // #FF9500 (Orange)
Color("DangerColor")     // #FF3B30 (Red)
```

---

## 11. 📦 Workspace vs Project

### Decision Matrix

| Use Case | Recommendation | Format |
|----------|---------------|--------|
| Single app | `.xcodeproj` | Project |
| Multiple related projects | `.xcworkspace` | Workspace |
| App + Framework | `.xcworkspace` | Workspace |
| App + CLI tool | `.xcworkspace` | Workspace |

### Current Structure: `.xcodeproj`

**Single project with multiple targets**

```
SecurityTool.xcodeproj
├── SecurityTool (App)
├── SecurityToolTests (Unit Tests)
└── SecurityToolUITests (UI Tests)
```

### Future Migration: `.xcworkspace`

**When to consider:**
- Adding a CLI tool version
- Creating reusable framework modules
- Splitting into multiple projects

```
SecurityTool.xcworkspace/
│
├── 📱 SecurityTool.xcodeproj        # Main macOS app
│   ├── SecurityTool (App Target)
│   └── Tests
│
├── 🔧 SecurityToolCore.xcodeproj    # Core framework
│   ├── SecurityToolCore (Framework)
│   └── SecurityToolCoreTests
│
└── 💻 SecurityToolCLI.xcodeproj     # Command-line tool
    ├── SecurityToolCLI (CLI Target)
    └── Tests
```

---

## 12. 📐 Code Organization Best Practices

### File Naming Conventions

| File Type | Convention | Example |
|-----------|-----------|---------|
| **Swift Files** | PascalCase | `AARAnalyzer.swift` |
| **Test Files** | `[Name]Tests.swift` | `AARAnalyzerTests.swift` |
| **Resource Files** | kebab-case | `app-icon.png` |
| **Protocol Files** | `[Name].swift` | `Analyzable.swift` |
| **Extension Files** | `[Type]+[Feature].swift` | `String+Validation.swift` |

### Group Organization Strategy

✅ **Recommended Structure:**

```
Group by Feature → Group by Type
│
├── Analysis Feature
│   ├── AARAnalyzer.swift (Logic)
│   ├── AnalysisViewModel.swift (ViewModel)
│   └── AnalysisView.swift (View)
│
└── Export Feature
    ├── ExportService.swift (Logic)
    ├── ExportViewModel.swift (ViewModel)
    └── ExportView.swift (View)
```

❌ **Avoid:**

```
Group by Type Only (hard to navigate)
│
├── Views/ (100+ files)
├── ViewModels/ (100+ files)
└── Services/ (100+ files)
```

### Target Membership Rules

| File Location | Target Membership |
|---------------|-------------------|
| `SecurityTool/` | ✓ SecurityTool |
| `SecurityToolTests/` | ✓ SecurityToolTests |
| `Shared/` | ✓ SecurityTool<br>✓ SecurityToolTests |
| `TestHelpers/` | ✓ SecurityToolTests |

### Code Organization Checklist

- [ ] Files grouped by feature
- [ ] Consistent naming conventions
- [ ] Clear folder structure
- [ ] Proper target membership
- [ ] No orphaned files
- [ ] Resources in asset catalog

---

## 13. 🔄 Development Workflow in Xcode

### Adding New Features - Step by Step

```
Step 1: Plan
│
├─→ Step 2: Create Feature Group
│   └─→ SecurityTool/Features/NewFeature/
│
├─→ Step 3: Add Models
│   └─→ Models/NewFeatureModel.swift
│
├─→ Step 4: Add Business Logic
│   └─→ Core/NewFeatureService.swift
│
├─→ Step 5: Add ViewModel
│   └─→ ViewModels/NewFeatureViewModel.swift
│
├─→ Step 6: Add View
│   └─→ Views/NewFeatureView.swift
│
├─→ Step 7: Write Tests
│   ├─→ Unit Tests
│   └─→ UI Tests
│
└─→ Step 8: Test & Refine
```

### File Template Usage

| Template | Use For | Shortcut |
|----------|---------|----------|
| **SwiftUI View** | Creating views | ⌘N → SwiftUI View |
| **Swift File** | Models, services | ⌘N → Swift File |
| **Unit Test Case** | Test classes | ⌘N → Unit Test Case |
| **Cocoa Class** | AppDelegate, etc. | ⌘N → Cocoa Class |

### Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Build | ⌘B |
| Run | ⌘R |
| Test | ⌘U |
| Clean Build Folder | ⌘⇧K |
| Show/Hide Navigator | ⌘0 |
| Show/Hide Debug Area | ⌘⇧Y |
| Quick Open | ⌘⇧O |

---

## 14. 🔀 Version Control Integration

### Git Integration in Xcode

**Built-in Features:**

| Feature | Access |
|---------|--------|
| View changes | Source Control Navigator (⌘2) |
| Commit | Source Control → Commit |
| Push/Pull | Source Control → Push/Pull |
| Branch management | Source Control → Branches |
| Merge conflicts | Automatic detection & resolution |

### `.gitignore` Configuration

**Recommended `.gitignore` for Xcode:**

```gitignore
# Xcode
#
# Gitignore for Xcode projects

## Build generated
build/
DerivedData/

## Various settings
*.pbxuser
!default.pbxuser
*.mode1v3
!default.mode1v3
*.mode2v3
!default.mode2v3
*.perspectivev3
!default.perspectivev3

## User-specific
xcuserdata/

## Workspace settings
*.xcworkspace/xcuserdata/

## Xcode Patch
*.xcodeproj/*
!*.xcodeproj/project.pbxproj
!*.xcodeproj/xcshareddata/

## Playgrounds
timeline.xctimeline
playground.xcworkspace

## Swift Package Manager
.build/
.swiftpm/

## CocoaPods (if used)
Pods/

## Provisioning
*.mobileprovision
*.cer
*.p12
*.certSigningRequest

## macOS
.DS_Store
```

### Commit Best Practices

✅ **Good Commits:**
```
feat: Add AAR file analyzer
fix: Resolve memory leak in parser
docs: Update README with installation steps
```

❌ **Bad Commits:**
```
update
fixed stuff
WIP
```

---

## 15. 📦 Distribution Setup

### Archive & Distribution Process

```
┌──────────────────┐
│  Development     │
│  Build & Test    │
└────────┬─────────┘
         │
┌────────▼─────────┐
│  Product →       │
│  Archive         │
└────────┬─────────┘
         │
┌────────▼─────────┐
│  Organizer       │
│  Select Archive  │
└────────┬─────────┘
         │
┌────────▼─────────┐
│  Distribute App  │
└────────┬─────────┘
         │
    ┌────┴────┐
    │         │
┌───▼──┐  ┌──▼────┐
│  App │  │ Dev   │
│ Store│  │  ID   │
└──────┘  └───────┘
```

### Distribution Methods

| Method | Use Case | Notarization |
|--------|----------|--------------|
| **Mac App Store** | App Store distribution | Automatic |
| **Developer ID** | Direct distribution | Required |
| **Development** | Internal testing | Not required |
| **Copy App** | Local testing | Not required |

### Notarization Process

**Command-line notarization:**

```bash
# Step 1: Archive and export
xcodebuild archive -scheme SecurityTool

# Step 2: Submit for notarization
xcrun notarytool submit SecurityTool.app \
  --apple-id "your-email@example.com" \
  --password "app-specific-password" \
  --team-id "TEAM_ID" \
  --wait

# Step 3: Staple the notarization
xcrun stapler staple SecurityTool.app
```

### Pre-Distribution Checklist

- [ ] All tests passing
- [ ] Code signing configured
- [ ] App sandbox enabled
- [ ] Privacy policy ready (if collecting data)
- [ ] App icon at all sizes
- [ ] Copyright information updated
- [ ] Version and build numbers set
- [ ] Release notes prepared

---

## 📊 Summary

### Project Structure Highlights

| Aspect | Implementation |
|--------|----------------|
| **Architecture** | MVVM with clean separation |
| **Language** | Swift 5.9+ |
| **UI Framework** | SwiftUI |
| **Testing** | Unit + UI test coverage |
| **Dependencies** | Swift Package Manager |
| **Target** | macOS 13.0+ |

### Key Benefits

✅ **Modularity**
- Clear separation of concerns (Core, Views, Services)
- Easy to add new analyzers and features
- Reusable components

✅ **Testability**
- Dedicated test targets with comprehensive coverage
- Mock implementations for testing
- Continuous integration ready

✅ **Scalability**
- Easy to extend with new file formats
- Can migrate to workspace for multi-project setup
- Framework-ready architecture

✅ **Maintainability**
- Clear file organization and naming conventions
- Well-documented structure
- Professional development practices

---

## 🚀 Next Steps

### Phase 1: Project Setup
1. Create `.xcodeproj` file in Xcode
2. Configure targets and schemes
3. Set up Swift Package Manager dependencies
4. Configure entitlements and signing

### Phase 2: Core Implementation
1. Implement file parsers (ZIP, Manifest, DEX)
2. Build analysis engines (AAR, JAR, APK)
3. Create security analyzers
4. Develop data models

### Phase 3: UI Development
1. Design and implement SwiftUI views
2. Create view models
3. Build reusable components
4. Implement file drag & drop

### Phase 4: Testing & Polish
1. Write comprehensive unit tests
2. Create UI tests
3. Performance optimization
4. Documentation

### Phase 5: Distribution
1. Prepare app for distribution
2. Set up code signing
3. Submit for notarization
4. Release!

---

> **Ready to build?** This structure provides everything needed to create a professional macOS security analysis tool. Start by creating the Xcode project and implementing the core analysis engines.
