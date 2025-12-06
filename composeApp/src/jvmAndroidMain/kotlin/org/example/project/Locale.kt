package org.example.project
import java.util.Locale

actual fun getCurrentLanguage(): String {
    return Locale.getDefault().language
}

actual fun setAppLanguage(language: String) {
    Locale.setDefault(Locale(language))
}
