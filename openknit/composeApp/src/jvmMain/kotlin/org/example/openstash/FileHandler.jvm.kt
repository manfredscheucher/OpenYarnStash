package org.example.openstash

import java.io.File

class JvmFileHandler : FileHandler {
    private val appDirName = ".openstash"
    private val fileName = "stash.json"
    private val userHome = System.getProperty("user.home")
    private val appDir = File(userHome, appDirName)
    private val file = File(appDir, fileName)

    init {
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
    }

    override suspend fun readFile(): String {
        return if (file.exists()) file.readText() else ""
    }

    override suspend fun writeFile(content: String) {
        file.writeText(content)
    }
}
