package org.example.project

import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

private const val LANGUAGE_KEY = "app_language"

actual fun getCurrentLanguage(): String {
    return localStorage[LANGUAGE_KEY] ?: "en"
}

actual fun setAppLanguage(language: String) {
    localStorage[LANGUAGE_KEY] = language
}
