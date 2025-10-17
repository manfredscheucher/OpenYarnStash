package org.example.project

interface FileHandler {
    suspend fun readFile(): String
    suspend fun writeFile(content: String)
    suspend fun backupFile(): String?
}
