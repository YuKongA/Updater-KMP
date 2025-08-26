# Copilot Instructions for Updater-KMP

## Repository Summary

**Updater-KMP** is a Kotlin Multiplatform application using Compose Multiplatform to get Xiaomi official recovery ROM information. It supports **Android**, **Desktop (JVM)**, **iOS**, **macOS**, **JS**, and **WasmJS** platforms.

The app allows users to:
- Get detailed information about Xiaomi ROM releases (public/beta/dev versions)
- Use device auto-detection with `AUTO` suffix (e.g., `OS2.0.100.0.AUTO`)
- Access different ROM sources based on login status
- Download and analyze ROM payloads and partitions

## High-Level Repository Information

- **Project Type**: Kotlin Multiplatform mobile/desktop application
- **UI Framework**: Compose Multiplatform
- **Primary Language**: Kotlin
- **Build System**: Gradle with Kotlin DSL
- **Target Platforms**: Android, Desktop (JVM), iOS, macOS, JS, WasmJS
- **Repository Size**: ~50 Kotlin source files across multiple platform source sets
- **Key Dependencies**: Ktor (HTTP client), kotlinx.serialization, cryptography-kotlin, miuix (UI library), Haze (blur effects)

**Toolchain Requirements:**
- Java/Kotlin toolchain: Java 21
- Gradle version: 9.0.0
- Android: compileSdk 36, targetSdk 36, minSdk 26
- Kotlin version: 2.2.10

## Build Instructions and Known Issues

### ⚠️ Critical Build Issue

**KNOWN ISSUE**: The repository currently has multiple build configuration problems that prevent local builds.

**Problems**:
1. `settings.gradle.kts` includes `com.android.settings` plugin version 8.11.0 which doesn't exist in repositories
2. `gradle/libs.versions.toml` specifies Android Gradle Plugin version 8.12.1 which doesn't exist
3. Environment/repository access issues preventing plugin resolution

**Current Build Status**: ❌ **Cannot build locally** due to missing plugin dependencies.

**GitHub Actions CI Status**: ✅ **Works correctly** - The CI environment resolves these issues, indicating this is a recent regression or environment-specific problem.

### Recommended Approach for Coding Agents

**For non-build related changes** (UI, business logic, data structures):
1. Make code changes directly without attempting local builds
2. Rely on GitHub Actions CI for validation
3. Use static analysis and code review instead of local testing

**For build-related changes**:
1. **DO NOT** attempt to fix the build issues unless that's the specific task
2. Changes to build files should be minimal and only if absolutely necessary
3. Reference the working CI configuration in `.github/workflows/Action CI.yml`

### Build Commands (When Working)

**These commands work in the CI environment but may fail locally:**

**Desktop (Linux/Windows):**
```bash
./gradlew createReleaseDistributable
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
- Java 21 (as specified in CI)
- For Android builds with release signing, environment variables needed:
  - `KEYSTORE_PATH`: Path to keystore file
  - `KEYSTORE_PASS`: Keystore password  
  - `KEY_ALIAS`: Key alias
  - `KEY_PASSWORD`: Key password

**Build Times (from CI):**
- Desktop build: ~3-5 minutes
- Android build: ~5-7 minutes  
- macOS build: ~5-8 minutes

### Testing

**No explicit test configuration found** in the repository. The project relies on GitHub Actions CI for validation across multiple platforms.

### Linting/Code Quality

**No explicit linting configuration found**. The project uses standard Kotlin code formatting.

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
- **ROM Processing**: `AppUtils.kt` handles ROM info retrieval and processing
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

1. **Always test the build fix first** by commenting out the problematic Android settings plugin
2. **Platform-specific testing**: Use the appropriate gradlew command for your target platform
3. **Cross-platform validation**: Changes to `commonMain` should be tested on multiple platforms
4. **ROM processing logic**: Test with valid device codes from `DeviceInfoHelper.kt`
5. **UI changes**: Test on both desktop and mobile form factors

### Dependencies Not Obvious from Layout

- **Protocol Buffers**: Used for ROM metadata parsing (see `protobuf-codegen/`)
- **Native Libraries**: JNA for platform-specific operations on desktop
- **Compression Libraries**: Apache Commons Compress and XZ for ROM archive handling
- **Cryptography**: Multi-platform crypto operations for ROM verification
- **Device Database**: Large embedded device list in `DeviceInfoHelper.kt` (150+ Xiaomi devices with codes)

### Important Implementation Details

- **Platform-specific implementations** use Kotlin's `expect`/`actual` mechanism
- **File operations vary significantly** between platforms (see `platform/FileSystem.*`)
- **HTTP clients differ** per platform (CIO for Android/JVM, Darwin for iOS/macOS, JS for web)
- **UI theming** supports system dark mode detection across all platforms
- **Device auto-detection** relies on hardcoded device codes in `DeviceInfoHelper.kt` - contains ~150 Xiaomi devices
- **ROM version formats**: Supports `AUTO` suffix (e.g., `OS2.0.100.0.AUTO`) for auto-completion
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
// Version transformation logic:
val systemVersionExt = systemVersion.value.uppercase()
    .replace("^OS1".toRegex(), "V816")
    .replace("AUTO$".toRegex(), deviceCode)
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
├── settings.gradle.kts (Project structure and repositories - HAS KNOWN ISSUES)
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

- **`misc/AppUtils.kt`** (426 lines): Core ROM processing, device detection, download URL generation
- **`PayloadAnalyzer.kt`** (328 lines): Advanced ROM payload extraction and analysis  
- **`data/DeviceInfoHelper.kt`** (500+ lines): Complete Xiaomi device database with codes
- **`misc/MessageUtils.kt`**: Toast and snackbar message handling
- **`misc/PartitionDownloadManager.kt`**: Parallel download manager for ROM partitions

## Important Notes for Coding Agents

1. **Build issues are environment-specific** - The repository builds successfully in CI but has local build problems
2. **Don't fix build issues unless specifically asked** - Focus on code changes and rely on CI for validation  
3. **Use platform-appropriate commands in CI context** - The documented commands work in the CI environment
4. **Expect/Actual pattern** - Platform-specific code uses Kotlin MPP's expect/actual mechanism
5. **Device codes are crucial** - When working with ROM detection, reference `DeviceInfoHelper.kt` for valid device codes
6. **Large codebase** - Focus changes on specific modules rather than broad refactoring
7. **No unit tests** - Rely on build success and CI validation rather than test suites
8. **Static analysis preferred** - Use code review and static analysis instead of local builds for validation

**Trust these instructions** - they are based on comprehensive repository analysis. The build issues documented here are real and should not be "fixed" unless that is the specific task assigned.