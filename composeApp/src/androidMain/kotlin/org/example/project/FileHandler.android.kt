package org.example.project

import android.content.Context
import java.io.File

class AndroidFileHandler(private val context: Context) : FileHandler {
    private val file = File(context.filesDir, "stash.json")

    override suspend fun readFile(): String {
        return if (file.exists()) file.readText() else ""
    }

    override suspend fun writeFile(content: String) {
        file.writeText(content)
    }
}
