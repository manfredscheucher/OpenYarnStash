package org.example.project

import kotlinx.browser.localStorage

class JsFileHandler : FileHandler {
    private val key = "database.json"

    override suspend fun readFile(): String {
        return localStorage.getItem(key) ?: ""
    }

    override suspend fun writeFile(content: String) {
        localStorage.setItem(key, content)
    }
}
