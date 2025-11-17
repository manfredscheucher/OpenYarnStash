package org.example.project

import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.write

object Logger {

    private const val LOG_FILE = "files/log.txt"

    fun log(message: String) {
        val timestamp = getCurrentTimestamp()
        val logMessage = "$timestamp: $message\n"

        println(logMessage)

        try {
            val path = Path(LOG_FILE)
            val parent = path.parent!!
            if (!SystemFileSystem.exists(parent)) {
                SystemFileSystem.createDirectories(parent)
            }
            if (!SystemFileSystem.exists(path)) {
                SystemFileSystem.createFile(path)
            }
            SystemFileSystem.sink(path, append = true).write(logMessage.encodeToByteArray()) {
            }
        } catch (e: Exception) {
            println("Error writing to log file: ${e.message}")
        }
    }
}