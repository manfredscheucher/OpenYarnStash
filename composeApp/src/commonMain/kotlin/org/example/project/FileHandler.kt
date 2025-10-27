package org.example.project

interface FileHandler {
    suspend fun readFile(path: String): String
    suspend fun writeFile(path: String, content: String)
    suspend fun backupFile(path: String): String?
    suspend fun writeBytes(path: String, bytes: ByteArray)
    suspend fun readBytes(path: String): ByteArray?
    suspend fun deleteFile(path: String)
    fun createTimestampedFileName(baseName: String, extension: String): String
}
