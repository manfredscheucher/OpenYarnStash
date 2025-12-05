package org.example.project

actual fun getCurrentLanguage(): String {
    return "en"
}

actual fun setAppLanguage(language: String) {
    // Not supported on wasm-js
}
