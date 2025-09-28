package org.example.openstash

import kotlinx.browser.localStorage
import org.w3c.dom.Storage

class WasmJsFileHandler : FileHandler {
    private val storageKey = "openstash_data_wasm"

    override suspend fun readFile(): String {
        return localStorage.getItem(storageKey) ?: ""
    }

    override suspend fun writeFile(content: String) {
        localStorage.setItem(storageKey, content)
    }
}
