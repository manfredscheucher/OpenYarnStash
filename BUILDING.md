# Building OpenYarnStash

This is a Kotlin Multiplatform project targeting **Android**, **iOS**, **Web**, and **Desktop (JVM)**.

## Project layout

- **[/composeApp](./composeApp/src)** contains code shared across Compose Multiplatform targets.
  - **[commonMain](./composeApp/src/commonMain/kotlin)** – code common to all targets.
  - Other folders contain Kotlin code compiled only for the platform indicated by the folder name.
    - For example, iOS-specific APIs belong in **[iosMain](./composeApp/src/iosMain/kotlin)**.
    - Desktop (JVM) specifics go to **[jvmMain](./composeApp/src/jvmMain/kotlin)**.
- **[/iosApp](./iosApp/iosApp)** contains the iOS application entry point (add SwiftUI code here if needed).

## Build & Run

### Android
Use your IDE’s run configuration or build from the terminal:

- macOS/Linux:
  ```bash
  ./gradlew :composeApp:assembleDebug
  ```

- Windows:
  ```bash
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Desktop (JVM)

Run via IDE or terminal:
- macOS/Linux:
  ```bash
  ./gradlew :composeApp:run
  ```

- Windows:
  ```bash
  .\gradlew.bat :composeApp:run
  ```

### Web

Run via IDE or terminal.
- Wasm target (faster, modern browsers):
  - macOS/Linux:
    ```bash
    ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
    ```

  - Windows:
    ```bash
    .\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun
    ```

- JS target (slower, supports older browsers):
  - macOS/Linux:
    ```bash
    ./gradlew :composeApp:jsBrowserDevelopmentRun
    ```

  - Windows:
    ```bash
    .\gradlew.bat :composeApp:jsBrowserDevelopmentRun
    ```

### iOS

Use your IDE’s run configuration or open /iosApp in Xcode and run from there.

## Learn more
- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform)
- [Kotlin/Wasm](https://kotl.in/wasm/)

## Feedback & issues

Please open an issue in this repository or contact us by email. We appreciate your feedback and contributions!
