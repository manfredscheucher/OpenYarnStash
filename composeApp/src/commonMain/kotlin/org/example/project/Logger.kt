package org.example.project

class Logger(private val fileHandler: FileHandler) {

    private val logFilePath = "log.txt"
    private val stashFilePath = "stash.json"
    private val settingsFilePath = "settings.json"
    private val filesDirPath = "."

    suspend fun log(message: String, logFiles: Boolean = false) {
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

        if (logFiles) {
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
        log("Content of $filePath:\n$content")
    }

    private suspend fun logDirectoryContents(dirPath: String) {
        val fileList = try {
            fileHandler.listFilesRecursively(dirPath)
        } catch (e: Exception) {
            listOf("Error listing directory: ${e.message}")
        }
        log("Recursive listing of '$dirPath':\n${fileList.joinToString("\n")}")
    }
}
