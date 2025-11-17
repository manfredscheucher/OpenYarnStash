package org.example.project

class Logger(private val fileHandler: FileHandler, private val settings: Settings) {

    private val logFilePath = "files/log.txt"
    private val stashFilePath = "files/stash.json"
    private val settingsFilePath = "files/settings.json"
    private val filesDirPath = "files"

    suspend fun log(message: String, level: LogLevel = LogLevel.VERBOSE, logFiles: Boolean = false) {
        if (settings.logLevel == LogLevel.DISABLED) return
        if (settings.logLevel.ordinal < level.ordinal) return

        val timestamp = getCurrentTimestamp()
        val logMessage = "$timestamp: $message\n"

        println(logMessage)

        try {
            val existingContent = try {
                fileHandler.readBytes(logFilePath)
            } catch (e: Exception) {
                null
            }

            val newContent = if (existingContent != null) {
                existingContent + logMessage.encodeToByteArray()
            } else {
                logMessage.encodeToByteArray()
            }
            fileHandler.writeBytes(logFilePath, newContent)
        } catch (e: Exception) {
            println("Error writing to log file: ${e.message}")
        }

        if (logFiles && settings.logLevel == LogLevel.VERBOSE) {
            logFileContent(stashFilePath)
            logFileContent(settingsFilePath)
            logDirectoryContents(filesDirPath)
        }
    }

    private suspend fun logFileContent(filePath: String) {
        val content = try {
            fileHandler.readFile(filePath)
        } catch (e: Exception) {
            "Error reading file: ${e.message}"
        }
        log("Content of $filePath:\n$content", LogLevel.VERBOSE)
    }

    private suspend fun logDirectoryContents(dirPath: String) {
        val fileList = try {
            fileHandler.listFilesRecursively(dirPath)
        } catch (e: Exception) {
            listOf("Error listing directory: ${e.message}")
        }
        log("Recursive listing of '$dirPath':\n${fileList.joinToString("\n")}", LogLevel.VERBOSE)
    }
}
