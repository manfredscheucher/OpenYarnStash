package org.example.project

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString

object Logger {

    private const val LOG_FILE = "files/log.txt"
    private const val STASH_FILE = "files/stash.json"
    private const val SETTINGS_FILE = "files/settings.json"
    private const val FILES_DIR = "files"

    fun log(message: String, logFiles: Boolean = false) {
        val timestamp = getCurrentTimestamp()
        val logMessage = "$timestamp: $message\n"

        println(logMessage)

        try {
            val path = Path(LOG_FILE)
            val parent = path.parent!!
            if (!SystemFileSystem.exists(parent)) {
                SystemFileSystem.createDirectories(parent, false)
            }
            SystemFileSystem.sink(path, append = true).buffered().use {
                it.write(logMessage.encodeToByteArray())
            }
        } catch (e: Exception) {
            println("Error writing to log file: ${e.message}")
        }

        if (logFiles) {
            printFileContent(STASH_FILE)
            printFileContent(SETTINGS_FILE)
            printDirectoryContents(FILES_DIR)
        }
    }

    private fun printFileContent(filePath: String) {
        val path = Path(filePath)
        println("\n--- Content of $filePath ---")
        try {
            if (SystemFileSystem.exists(path)) {
                val content = SystemFileSystem.source(path).buffered().use { it.readString() }
                println(content)
            } else {
                println("File not found.")
            }
        } catch (e: Exception) {
            println("Error reading file: ${e.message}")
        }
        println("--- End of $filePath ---\n")
    }

    private fun printDirectoryContents(dirPath: String) {
        val path = Path(dirPath)
        println("\n--- Recursive listing of '$dirPath' ---")
        try {
            val metadata = SystemFileSystem.metadataOrNull(path)
            if (metadata?.isDirectory == true) {
                listDirectoryRecursively(path, "")
            } else {
                println("Directory not found or not a directory.")
            }
        } catch (e: Exception) {
            println("Error listing directory: ${e.message}")
        }
        println("--- End of listing ---\n")
    }

    private fun listDirectoryRecursively(dir: Path, prefix: String) {
        try {
            SystemFileSystem.list(dir).forEach { entry ->
                val metadata = SystemFileSystem.metadataOrNull(entry)
                if (metadata?.isDirectory == true) {
                    println("$prefix${entry.name}/")
                    listDirectoryRecursively(entry, "$prefix  ")
                } else {
                    println("$prefix${entry.name}")
                }
            }
        } catch (e: Exception) {
            println("$prefix  -> Error: ${e.message}")
        }
    }
}
