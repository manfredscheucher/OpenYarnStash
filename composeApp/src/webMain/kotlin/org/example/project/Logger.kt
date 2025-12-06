package org.example.project

actual fun initializeLogger(fileHandler: FileHandler, settings: Settings) {
    // Not supported on wasm-js
}
