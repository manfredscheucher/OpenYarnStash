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

    override suspend fun backupFile() {
        val content = localStorage.getItem(key)
        if (content != null) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            val timestamp = "${now.year}${now.monthNumber.toString().padStart(2, '0')}${now.dayOfMonth.toString().padStart(2, '0')}-"
            + "${now.hour.toString().padStart(2, '0')}${now.minute.toString().padStart(2, '0')}${now.second.toString().padStart(2, '0')}"
            val backupKey = "$key.backup.$timestamp"
            localStorage.setItem(backupKey, content)
            localStorage.removeItem(key)
        }
    }
}
