package org.example.project

import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDefaultsDidChangeNotification
import platform.Foundation.NSObject
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotification
import platform.Foundation.preferredLanguages
import kotlin.text.substringBefore

// Schlüssel zum Speichern der ausgewählten Sprache
private const val APPLE_LANGUAGE_KEY = "AppleLanguages"

actual fun getCurrentLanguage(): String {
    // 1. Prüfen, ob der Nutzer bereits eine Sprache in der App gewählt hat
    val savedLanguage = NSUserDefaults.standardUserDefaults.stringArrayForKey(APPLE_LANGUAGE_KEY)?.firstOrNull() as? String
    if (savedLanguage != null) {
        return savedLanguage.substringBefore("-")
    }

    // 2. Ansonsten die bevorzugte Systemsprache nehmen
    val preferredLanguage = NSLocale.preferredLanguages.firstOrNull() as? String
    return preferredLanguage?.substringBefore("-") ?: "en" // Fallback auf "en"
}

actual fun setAppLanguage(language: String) {
    // Speichere die neue Sprache an erster Stelle der Prioritätenliste
    NSUserDefaults.standardUserDefaults.setObject(kotlin.collections.listOf(language), forKey = APPLE_LANGUAGE_KEY)

    // WICHTIG: Die App muss neu gestartet werden, damit die Änderung wirksam wird.
    // Compose Multiplatform kann (derzeit) die Ressourcen nicht ohne Neustart komplett neu laden.
    // Du solltest dem Nutzer eine entsprechende Nachricht anzeigen.
}
