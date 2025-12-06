package org.example.project

/**
 * A platform-specific implementation for downloading or sharing a file.
 */
expect class FileDownloader {
    fun download(fileName: String, data: ByteArray, context: Any? = null)
}
