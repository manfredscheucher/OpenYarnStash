package org.example.project

interface FileHandler {
    suspend fun readFile(): String
    suspend fun writeFile(content: String)
    suspend fun backupFile(): String?
    suspend fun writeBytes(path: String, bytes: ByteArray)
    suspend fun readBytes(path: String): ByteArray?
    suspend fun deleteFile(path: String)
}
