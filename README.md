# Security Tool - Archive Importer

A Kotlin desktop application for importing and extracting Android/Java archive files (APK, AAR, AAB, JAR) for security analysis.

## Features

- 🎯 **Multi-Format Support**: Import and extract APK, AAR, AAB, and JAR files
- 🖥️ **Modern Desktop UI**: Built with Compose for Desktop
- 📦 **Archive Analysis**: View archive information before extraction
- 🔍 **Security-Focused**: Includes zip-slip protection and safe extraction
- 📊 **Progress Tracking**: Real-time extraction progress and logs
- ✅ **Cross-Platform**: Runs on Windows, macOS, and Linux

## Supported File Formats

- **APK** - Android Application Package
- **AAR** - Android Archive Library
- **AAB** - Android App Bundle
- **JAR** - Java Archive

## Requirements

- Java JDK 17 or higher
- Gradle 8.5+ (included via wrapper)

## Building the Application

### Using Gradle Wrapper (Recommended)

```bash
# On Linux/macOS
./gradlew build

# On Windows
gradlew.bat build
```

### Running the Application

```bash
# On Linux/macOS
./gradlew run

# On Windows
gradlew.bat run
```

### Creating Distribution Package

```bash
# Create native distribution for your platform
./gradlew packageDistributionForCurrentOS

# Create DMG (macOS)
./gradlew packageDmg

# Create MSI (Windows)
./gradlew packageMsi

# Create DEB (Linux)
./gradlew packageDeb
```

The distribution packages will be created in `build/compose/binaries/main/`

## Usage

1. **Launch the application**
   - Run using Gradle: `./gradlew run`
   - Or use the packaged distribution

2. **Select Archive File**
   - Click "Select Archive File" button
   - Choose an APK, AAR, AAB, or JAR file from your file system

3. **View Archive Information**
   - The app displays file details and entry count
   - Preview shows the archive structure

4. **Extract Archive**
   - Click "Extract Archive" button
   - Files will be extracted to a folder named `[filename]_extracted`
   - Progress log shows extraction status

5. **View Results**
   - Success message shows number of files extracted
   - Output directory path is displayed
   - Open the extracted folder to analyze contents

## Project Structure

```
security-tool/
├── src/main/kotlin/
│   ├── Main.kt                    # Application entry point
│   ├── ui/
│   │   └── ArchiveImporter.kt     # UI components and layout
│   └── utils/
│       └── ArchiveExtractor.kt    # Archive extraction logic
├── build.gradle.kts               # Gradle build configuration
├── settings.gradle.kts            # Gradle settings
└── README.md                      # This file
```

## Security Features

- **Zip-Slip Protection**: Prevents malicious archives from extracting files outside the target directory
- **Safe File Operations**: Validates file paths and prevents directory traversal attacks
- **Archive Validation**: Checks file extensions and validates archive format

## Development

### IDE Setup

The project can be opened in IntelliJ IDEA or any Kotlin-compatible IDE:

1. Open the project directory
2. The IDE should automatically detect Gradle configuration
3. Wait for Gradle sync to complete
4. Run the `main()` function in `Main.kt`

### Technology Stack

- **Language**: Kotlin 1.9.0
- **UI Framework**: Compose for Desktop 1.5.1
- **Build Tool**: Gradle 8.5
- **Archive Handling**: Java ZIP utilities

## Troubleshooting

### Java Version Issues
Ensure you have JDK 17 or higher installed:
```bash
java -version
```

### Gradle Build Fails
Try cleaning and rebuilding:
```bash
./gradlew clean build
```

### UI Doesn't Appear
Make sure you have a display environment set up. On Linux, ensure X11 or Wayland is running.

## License

This project is a security analysis tool for educational and authorized testing purposes.

## Contributing

Contributions are welcome! Please ensure any changes maintain the security-focused nature of the tool.
