# Building OpenYarnStash

This guide provides instructions on how to build and run the OpenYarnStash project on its supported platforms: **Android**, **iOS**, **Web**, and **Desktop (JVM)**.

## Project Layout

- **/composeApp**: Contains the shared Kotlin Multiplatform source code.
  - **commonMain**: Code common to all targets.
  - **androidMain**, **iosMain**, etc.: Platform-specific code.
- **/iosApp**: The iOS application entry point and Xcode project.

## Build & Run

You can build and run the project directly from your IDE (like Android Studio) or use the Gradle wrapper (`gradlew`) from your terminal.

*On Windows, use `gradlew.bat` instead of `./gradlew` for all terminal commands.*

### Android

To build the debug version of the app, run:

```bash
./gradlew :composeApp:assembleDebug
```

### Desktop (JVM)

To run the desktop application:

```bash
./gradlew :composeApp:run
```

### Web

Run one of the following commands to start the development server:

- **Wasm (recommended for modern browsers):**
  
  ```bash
  ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
  ```

- **JS (for broader browser compatibility):**
  
  ```bash
  ./gradlew :composeApp:jsBrowserDevelopmentRun
  ```

### iOS

Open the `/iosApp` directory in Xcode and run the project from there.

#### iOS ZIP Support

The iOS target uses native Foundation APIs (NSInputStream/NSOutputStream) for ZIP file handling with zlib compression. The implementation uses:
- **Native iOS APIs**: No third-party dependencies required
- **zlib**: Standard system library (automatically available on iOS/macOS)
- **C Interop**: Configured in `build.gradle.kts` via cinterop for minizip

No additional setup is required - zlib is included in all iOS/macOS systems by default.

## Running Tests

The project includes multiplatform unit tests that verify core functionality across all platforms.

### Run All Tests

To run all unit tests across all platforms:

```bash
./gradlew :composeApp:allTests
```

### Run Platform-Specific Tests

To run tests for a specific platform:

```bash
# JVM/Desktop tests
./gradlew :composeApp:jvmTest

# Android tests
./gradlew :composeApp:testDebugUnitTest

# iOS tests
./gradlew :composeApp:iosSimulatorArm64Test
```

### Test Coverage

The test suite includes:
- **ZIP Export/Import**: Verifies file creation, export to ZIP, deletion, and import from ZIP
- **Empty Directory Handling**: Tests exporting empty directories
- **Binary Data**: Validates binary file preservation through ZIP compression cycle

Tests use temporary directories to avoid affecting real user data.

## Learn More

- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/)
- [Kotlin/Wasm](https://kotl.in/wasm/)
