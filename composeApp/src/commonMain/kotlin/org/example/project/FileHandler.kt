package org.example.project

expect class FileInputSource

expect abstract class OutputStream() {
    abstract fun write(b: Int)
    open fun write(b: ByteArray, off: Int, len: Int)
    open fun flush()
    open fun close()
}

interface FileHandler {
    suspend fun readText(path: String): String
    suspend fun writeText(path: String, content: String)
    suspend fun appendText(path: String, content: String)
    suspend fun backupFile(path: String): String?
    suspend fun writeBytes(path: String, bytes: ByteArray)
    suspend fun readBytes(path: String): ByteArray?
    fun openInputStream(path: String): FileInputSource?
    suspend fun deleteFile(path: String)
    suspend fun zipFiles(): ByteArray
    suspend fun renameFilesDirectory(newName: String)
    suspend fun unzipAndReplaceFiles(zipInputStream: Any)
    fun createTimestampedFileName(baseName: String, extension: String): String
    suspend fun listFilesRecursively(path: String): List<String>
    suspend fun getFileHash(path: String): String?

    fun openFileExternally(path: String)
}
