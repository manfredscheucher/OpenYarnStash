package org.example.project

import java.io.File

class JvmFileHandler : FileHandler {
    private val file = File("stash.json")

    override suspend fun readFile(): String {
        return if (file.exists()) {
            file.readText()
        } else {
            ""
        }
    }

    override suspend fun writeFile(content: String) {
        file.writeText(content)
    }
}
