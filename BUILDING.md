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

## Learn More

- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/)
- [Kotlin/Wasm](https://kotl.in/wasm/)
