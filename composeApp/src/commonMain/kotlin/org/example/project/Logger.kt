package org.example.project

class Logger(private val fileHandler: FileHandler, private val settings: Settings) {

    private val logFilePath = "log.txt"
    private val stashFilePath = "stash.json"
    private val settingsFilePath = "settings.json"
    private val filesDirPath = "."

    suspend fun log(level: LogLevel,message: String) {
        if (settings.logLevel == LogLevel.OFF) return
        if (level.ordinal > settings.logLevel.ordinal) return

        val timestamp = getCurrentTimestamp()
        val logMessage = "$timestamp: [${level.name}] $message\n"

        println(logMessage)

        try {
            fileHandler.appendText(logFilePath, logMessage)
        } catch (e: Exception) {
            println("Error writing to log file: ${e.message}")
        }
    }

    suspend fun logImportantFiles(level: LogLevel) {
        logFile(level,stashFilePath)
        logFile(level,settingsFilePath)
        logDirectoryContents(level,filesDirPath)
    }

    private suspend fun logFile(level: LogLevel,filePath: String) {
        val content = try {
            fileHandler.readText(filePath)
        } catch (e: Exception) {
            "Error reading file: ${e.message}"
        }
        log(level,"Content of $filePath:\n$content")
    }

    suspend fun logDirectoryContents(level: LogLevel,dirPath: String) {
        val fileList = try {
            fileHandler.listFilesRecursively(dirPath)
        } catch (e: Exception) {
            listOf("Error listing directory: ${e.message}")
        }

        val hashedFiles = fileList.map { filePath ->
            val hash = fileHandler.getFileHash(filePath) ?: "NO HASH"
            "$hash  $filePath"
        }

        log(level,"Recursive listing of '$dirPath':\n${hashedFiles.joinToString("\n")}")
    }
}
