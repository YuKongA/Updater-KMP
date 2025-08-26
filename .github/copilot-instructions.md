# Copilot Instructions for Updater-KMP

## Repository Summary

**Updater-KMP** is a Kotlin Multiplatform application using Compose Multiplatform to get Xiaomi official recovery ROM information. It supports **Android**, **Desktop (JVM)**, **iOS**, **macOS**, **JS**, and **WasmJS** platforms.

The app allows users to:
- Get detailed information about Xiaomi ROM releases (public/beta/dev versions)
- Use automatic device code completion with `AUTO` suffix (e.g., `OS2.0.100.0.AUTO`)
- Access authenticated ROM sources when logged in with Xiaomi account
- Download and analyze ROM payloads and partitions

## High-Level Repository Information

- **Project Type**: Kotlin Multiplatform mobile/desktop application
- **UI Framework**: Compose Multiplatform
- **Primary Language**: Kotlin
- **Build System**: Gradle with Kotlin DSL
- **Target Platforms**: Android, Desktop (JVM), iOS, macOS, JS, WasmJS
- **Repository Size**: Multiple Kotlin source files organized across platform-specific source sets
- **Key Dependencies**: Ktor (HTTP client), kotlinx.serialization, cryptography-kotlin, miuix (UI library), Haze (blur effects)

**Toolchain Requirements:**
- Java/Kotlin toolchain: Java 21
- Gradle version: 9.0.0
- Android: compileSdk 36, targetSdk 36, minSdk 26
- Kotlin version: 2.2.10

## Build Instructions

**Current Build Status**: ✅ **Builds successfully locally and in CI** - Dependency access issues have been resolved.

**GitHub Actions CI Status**: ✅ **Works correctly** - All platforms build successfully in CI environment.

### Recommended Approach for Coding Agents

**For all changes** (UI, business logic, data structures, build configurations):
1. Build and test changes locally using the commands below
2. Validate changes with appropriate platform-specific build commands before committing
3. GitHub Actions CI provides comprehensive validation across all supported platforms

**For build-related changes**:
1. Test locally using the appropriate build commands below
2. Changes to build files can be validated immediately with local builds
3. Reference the working CI configuration in `.github/workflows/Action CI.yml` for comprehensive platform testing

### Build Commands

**These commands work both locally and in CI:**

**Desktop (Linux/Windows):**
```bash
./gradlew desktopJar          # Build JAR file
./gradlew desktopRun          # Run the application
./gradlew createReleaseDistributable  # Create distributable package
# Artifact: composeApp/build/compose/binaries/main-release/app/Updater
```

**macOS:**
```bash
./gradlew packageDmgNativeReleaseMacosArm64
# Artifact: composeApp/build/compose/binaries/main/native-macosArm64-release-dmg
```

**Android:**
```bash
./gradlew assembleDebug    # Debug build
./gradlew assembleRelease  # Release build (requires signing key)
# Artifacts: composeApp/build/outputs/apk/debug/ and release/
```

**Environment Setup Requirements:**
- Java 17 or higher (Java 17 confirmed working, Java 21 recommended for CI compatibility)
- For Android builds with release signing, environment variables needed:
  - `KEYSTORE_PATH`: Path to keystore file
  - `KEYSTORE_PASS`: Keystore password  
  - `KEY_ALIAS`: Key alias
  - `KEY_PASSWORD`: Key password

**Build Times (estimated from CI):**
- Desktop build: ~3-5 minutes
- Android build: ~5-7 minutes  
- macOS build: ~5-8 minutes

### Testing

**No unit test suite configured** in the repository. Validate changes using:
- Local builds with the appropriate platform target (Android: `./gradlew assembleDebug`, Desktop: `./gradlew desktopJar`)
- GitHub Actions CI for comprehensive cross-platform validation

### Linting/Code Quality

**No explicit linting configuration** found. The project follows standard Kotlin code formatting conventions.

## Project Layout and Architecture

### Directory Structure

```
Updater-KMP/
├── .github/
│   └── workflows/
│       └── Action CI.yml          # CI pipeline for all platforms
├── composeApp/                     # Main application module
│   ├── build.gradle.kts           # Main build configuration
│   ├── src/
│   │   ├── androidMain/kotlin/    # Android-specific code
│   │   ├── commonMain/kotlin/     # Shared code across platforms
│   │   │   ├── App.kt            # Main app composition
│   │   │   ├── data/             # Data classes and helpers
│   │   │   │   ├── DataHelper.kt        # Core data structures
│   │   │   │   ├── DeviceInfoHelper.kt  # Device detection logic
│   │   │   │   ├── PayloadHelper.kt     # ROM payload analysis
│   │   │   │   └── RomInfoHelper.kt     # ROM information processing
│   │   │   ├── misc/             # Utility functions
│   │   │   │   ├── AppUtils.kt          # Core app utilities
│   │   │   │   ├── MessageUtils.kt      # Messaging/notifications
│   │   │   │   └── PartitionDownloadManager.kt # ROM download logic
│   │   │   ├── platform/         # Platform-specific abstractions
│   │   │   │   ├── HttpClient.kt        # HTTP client interface
│   │   │   │   ├── FileSystem.kt        # File operations
│   │   │   │   ├── Crypto.kt           # Cryptography operations
│   │   │   │   └── Toast.kt            # Toast notifications
│   │   │   └── ui/               # UI components
│   │   ├── desktopMain/kotlin/   # Desktop-specific implementations
│   │   ├── iosMain/kotlin/       # iOS-specific implementations
│   │   ├── macosMain/kotlin/     # macOS-specific implementations
│   │   ├── jsMain/kotlin/        # JavaScript web implementations
│   │   └── wasmJsMain/kotlin/    # WebAssembly implementations
├── iosApp/                        # iOS Xcode project
├── protobuf-codegen/              # Protocol buffer code generation
├── gradle/
│   └── libs.versions.toml         # Dependency version catalog
├── build.gradle.kts              # Root build configuration
└── settings.gradle.kts           # Gradle settings
```

### Key Configuration Files

- **`gradle/libs.versions.toml`**: Version catalog with all dependencies
- **`composeApp/build.gradle.kts`**: Main application build configuration with multiplatform targets
- **`composeApp/proguard-rules-*.pro`**: ProGuard rules for Android/JVM
- **`iosApp/`**: Complete iOS project structure with Podfile for CocoaPods

### Architectural Elements

**Core Application Structure:**
- `App.kt`: Main Compose UI entry point with navigation and theming
- `data/`: Data layer with ROM info processing, device detection, and API models
- `platform/`: Platform abstraction layer with expect/actual implementations
- `misc/`: Business logic utilities for ROM processing and downloads

**Key Business Logic:**
- **Device Detection**: `DeviceInfoHelper.kt` contains device codes and auto-completion logic
- **ROM Processing**: `AppUtils.kt` handles ROM information retrieval and processing
- **Payload Analysis**: `PayloadAnalyzer.kt` can analyze and extract ROM payload contents
- **Download Management**: `PartitionDownloadManager.kt` handles parallel ROM downloads

### GitHub Actions CI

**Workflow**: `.github/workflows/Action CI.yml`

**Triggers**: Push to main branch (excluding README.md, LICENSE changes)

**Build Matrix**:
- **macOS**: Builds DMG for macOS ARM64
- **Ubuntu**: Builds Linux x64 binary + Android APK (debug/release)
- **Ubuntu ARM64**: Builds Linux ARM64 binary
- **Windows**: Builds Windows x64 executable

**Artifacts Produced**:
- `Updater-darwin-arm64-dmg`
- `Updater-linux-x64-bin` 
- `Updater-linux-arm64-bin`
- `Updater-windows-x64-exe`
- `Updater-android-aarch64-apk`

### Validation Steps for Changes

1. **Local builds**: Use the appropriate gradlew command for your target platform
2. **Platform-specific testing**: 
   - Android: `./gradlew assembleDebug` or `./gradlew assembleRelease`
   - Desktop: `./gradlew desktopJar` or `./gradlew desktopRun`
   - Cross-platform: `./gradlew build` (excludes iOS/macOS on non-Mac systems)
3. **Cross-platform validation**: Changes to `commonMain` should be tested on multiple platforms
4. **ROM processing logic**: Test with valid device codes from `DeviceInfoHelper.kt`
5. **UI changes**: Test on both desktop and mobile form factors

### Dependencies Not Obvious from Layout

- **Protocol Buffers**: Used for ROM metadata parsing (see `protobuf-codegen/`)
- **Native Libraries**: JNA for platform-specific operations on desktop
- **Compression Libraries**: Apache Commons Compress and XZ for ROM archive handling
- **Cryptography**: Multi-platform crypto operations for ROM verification
- **Device Database**: Embedded device list in `DeviceInfoHelper.kt` (150+ Xiaomi devices with codes)

### Important Implementation Details

- **Platform-specific implementations** use Kotlin's `expect`/`actual` mechanism
- **File operations vary significantly** between platforms (see `platform/FileSystem.*`)
- **HTTP clients differ** per platform (CIO for Android/JVM, Darwin for iOS/macOS, JS for web)
- **UI theming** supports system dark mode detection across all platforms
- **Device auto-detection** relies on hardcoded device codes in `DeviceInfoHelper.kt` - contains 150+ Xiaomi devices
- **ROM version formats**: Supports `AUTO` suffix (e.g., `OS2.0.100.0.AUTO`) for automatic device code completion
- **Payload analysis**: Can extract and analyze partition data from Xiaomi ROM files

### Key Code Snippets

**Device Code Pattern (DeviceInfoHelper.kt)**:
```kotlin
// Device detection uses this format:
Device("Display Name", "codename", "deviceCode")
// Example: Device("Xiaomi 13", "fuxi", "MC")
```

**ROM Version Processing (AppUtils.kt)**:
```kotlin
// Version transformation logic that converts ROM versions and substitutes device codes:
val systemVersionExt = systemVersion.value.uppercase()
    .replace("^OS1".toRegex(), "V816")      // Convert OS1 prefix to V816
    .replace("AUTO$".toRegex(), deviceCode) // Replace AUTO suffix with actual device code
```

**Platform Abstraction Pattern**:
```kotlin
// In commonMain/platform/Toast.kt
expect fun showToast(message: String, duration: Long)

// In androidMain/platform/Toast.android.kt  
actual fun showToast(message: String, duration: Long) { /* Android implementation */ }
```

### Root Directory Files

```
├── LICENSE (Apache 2.0)
├── README.md (Basic usage instructions)
├── build.gradle.kts (Plugin declarations only)
├── settings.gradle.kts (Project structure and repositories)
├── gradle.properties (Gradle configuration with MPP optimizations)
├── gradlew / gradlew.bat (Gradle wrapper scripts)
└── .gitignore (Comprehensive ignore rules for all platforms)
```

### UI Component Structure

```
composeApp/src/commonMain/kotlin/ui/
├── App.kt                    # Main UI entry point with navigation
├── AboutDialog.kt            # About dialog with app info
├── BasicViews.kt             # Input forms for device selection
├── LoginCardView.kt          # Xiaomi account login UI
├── LoginDialog.kt            # Login dialog implementation
├── ResultViews.kt            # ROM information display
└── components/
    ├── AutoCompleteTextField.kt  # Device selection with auto-complete
    ├── PayloadDumperView.kt     # ROM payload analyzer interface
    └── TextWithIcon.kt          # Reusable UI components
```

### Key Business Logic Files

- **`misc/AppUtils.kt`**: Core ROM processing, device detection, download URL generation
- **`PayloadAnalyzer.kt`**: ROM payload extraction and analysis  
- **`data/DeviceInfoHelper.kt`**: Xiaomi device database with device codes
- **`misc/MessageUtils.kt`**: Toast and snackbar message handling
- **`misc/PartitionDownloadManager.kt`**: Parallel download manager for ROM partitions

## Important Notes for Coding Agents

1. **Builds work reliably** - The repository builds successfully both locally and in CI
2. **Validate with platform-specific commands** - Test changes locally with the documented build commands  
3. **Multi-platform support** - The documented commands work in both local and CI environments
4. **Expect/Actual pattern** - Platform-specific code uses Kotlin MPP's expect/actual mechanism
5. **Device codes are essential** - When working with ROM detection, reference `DeviceInfoHelper.kt` for valid device codes
6. **Modular architecture** - Focus changes on specific modules to maintain code organization
7. **Build-based validation** - Rely on build success and functional validation rather than unit test suites
8. **Local-first development** - Use local builds for immediate feedback, CI for comprehensive cross-platform testing

**Updated Status** - Dependency access issues have been resolved. Local development and testing are now fully supported.
