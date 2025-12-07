package org.example.project

actual fun getCurrentLanguage(): String {
    return js("navigator.language || navigator.userLanguage") as? String
        ?: "en"
}

actual fun setAppLanguage(language: String) {
    // In browsers, we can't actually change the language programmatically
    // This is a no-op in JS, as the browser language is determined by user settings
    console.log("Language change requested to: $language (not supported in browser)")
}
