package org.example.project

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

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

    override suspend fun backupFile() {
        if (file.exists()) {
            val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date())
            val backupFile = File("${file.name}.backup.$timestamp")
            file.renameTo(backupFile)
        }
    }
}
