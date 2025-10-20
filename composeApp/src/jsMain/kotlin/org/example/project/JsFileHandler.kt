package org.example.project

import kotlinx.browser.localStorage
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class JsFileHandler : FileHandler {
    private val key = "stash.json"

    override suspend fun readFile(): String {
        return localStorage.getItem(key) ?: ""
    }

    override suspend fun writeFile(content: String) {
        localStorage.setItem(key, content)
    }

    override suspend fun backupFile(): String? {
        val content = localStorage.getItem(key)
        if (content != null) {
            val timestamp = getCurrentTimestamp()
            val backupKey = "${key.substringBeforeLast('.')}-$timestamp.${key.substringAfterLast('.')}"
            localStorage.setItem(backupKey, content)
            return backupKey
        }
        return null
    }
}
