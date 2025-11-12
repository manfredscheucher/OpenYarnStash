package org.example.project

expect class FileInputSource

interface FileHandler {
    suspend fun readFile(path: String): String
    suspend fun writeFile(path: String, content: String)
    suspend fun backupFile(path: String): String?
    suspend fun writeBytes(path: String, bytes: ByteArray)
    suspend fun readBytes(path: String): ByteArray?
    fun openInputStream(path: String): FileInputSource?
    suspend fun deleteFile(path: String)
    suspend fun zipFiles(): ByteArray
    suspend fun renameFilesDirectory(newName: String)
    suspend fun unzipAndReplaceFiles(zipBytes: ByteArray)
    fun createTimestampedFileName(baseName: String, extension: String): String
}
