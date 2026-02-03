package org.example.project

import platform.Foundation.*
import kotlinx.cinterop.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
class IosFileHandler : FileHandler {
    private val fileManager = NSFileManager.defaultManager
    private val documentsDir = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).first() as NSURL
    private val filesDir = documentsDir.URLByAppendingPathComponent("files")!!

    init {
        // Create files directory if it doesn't exist
        if (!fileManager.fileExistsAtPath(filesDir.path ?: "")) {
            fileManager.createDirectoryAtURL(filesDir, true, null, null)
        }
    }

    private fun getURL(path: String): NSURL = filesDir.URLByAppendingPathComponent(path)!!

    private fun ensureParentDirectoryExists(url: NSURL) {
        val parentURL = url.URLByDeletingLastPathComponent
        if (parentURL != null) {
            val parentPath = parentURL.path
            if (parentPath != null && !fileManager.fileExistsAtPath(parentPath)) {
                fileManager.createDirectoryAtURL(parentURL, true, null, null)
            }
        }
    }

    override suspend fun readText(path: String): String {
        val url = getURL(path)
        if (!fileManager.fileExistsAtPath(url.path ?: "")) {
            return ""
        }
        return NSString.stringWithContentsOfURL(url, NSUTF8StringEncoding, null) as String? ?: ""
    }

    override suspend fun writeText(path: String, content: String) {
        val url = getURL(path)
        ensureParentDirectoryExists(url)
        (content as NSString).writeToURL(url, true, NSUTF8StringEncoding, null)
    }

    override suspend fun appendText(path: String, content: String) {
        val existing = readText(path)
        writeText(path, existing + content)
    }

    override suspend fun backupFile(path: String): String? {
        val timestamp = NSDate().timeIntervalSince1970.toLong()
        val backupPath = "${path}_$timestamp.bak"
        fileManager.copyItemAtURL(getURL(path), getURL(backupPath), null)
        return backupPath
    }

    override suspend fun writeBytes(path: String, bytes: ByteArray) {
        val url = getURL(path)
        ensureParentDirectoryExists(url)
        bytes.toNSData().writeToURL(url, true)
    }

    override suspend fun readBytes(path: String): ByteArray? {
        return NSData.dataWithContentsOfURL(getURL(path))?.toByteArray()
    }

    override fun openInputStream(path: String): FileInputSource? {
        val url = getURL(path)
        if (!fileManager.fileExistsAtPath(url.path ?: "")) {
            return null
        }
        return NSInputStream.inputStreamWithURL(url)
    }

    override suspend fun deleteFile(path: String) {
        fileManager.removeItemAtURL(getURL(path), null)
    }

    override suspend fun zipFiles(): ByteArray {
        val tempZipURL = documentsDir.URLByAppendingPathComponent("temp_export.zip")!!

        // Delete temp file if exists
        if (fileManager.fileExistsAtPath(tempZipURL.path ?: "")) {
            fileManager.removeItemAtURL(tempZipURL, null)
        }

        return try {
            val success = NativeZipSupport.createZipFromDirectory(filesDir, tempZipURL)
            if (!success) {
                return ByteArray(0)
            }

            // Read the archive file into ByteArray
            val archiveData = NSData.dataWithContentsOfURL(tempZipURL)
            val result = archiveData?.toByteArray() ?: ByteArray(0)

            // Clean up temp file
            fileManager.removeItemAtURL(tempZipURL, null)

            result
        } catch (e: Exception) {
            ByteArray(0)
        }
    }

    override suspend fun renameFilesDirectory(newName: String) { }

    override suspend fun restoreBackupDirectory(backupName: String) { }

    override suspend fun deleteFilesDirectory() {
        // Delete the files directory and recreate it
        if (fileManager.fileExistsAtPath(filesDir.path ?: "")) {
            fileManager.removeItemAtURL(filesDir, null)
        }
        fileManager.createDirectoryAtURL(filesDir, true, null, null)
    }

    override suspend fun deleteBackupDirectory(backupName: String) { }

    override suspend fun unzipAndReplaceFiles(zipInputStream: Any) {
        val tempZipURL = documentsDir.URLByAppendingPathComponent("temp_import.zip")!!

        when (zipInputStream) {
            is NSInputStream -> {
                try {
                    // Write stream to temp file
                    val outputStream = NSOutputStream.outputStreamToFileAtPath(tempZipURL.path!!, false)
                    if (outputStream == null) return

                    outputStream.open()
                    zipInputStream.open()

                    val buffer = ByteArray(8192)
                    buffer.usePinned { pinned ->
                        while (zipInputStream.hasBytesAvailable) {
                            val bytesRead = zipInputStream.read(pinned.addressOf(0).reinterpret(), 8192u)
                            if (bytesRead > 0) {
                                outputStream.write(pinned.addressOf(0).reinterpret(), bytesRead.toULong())
                            } else {
                                break
                            }
                        }
                    }

                    zipInputStream.close()
                    outputStream.close()

                    // Extract the archive
                    // First, clear existing files directory
                    if (fileManager.fileExistsAtPath(filesDir.path ?: "")) {
                        fileManager.removeItemAtURL(filesDir, null)
                        fileManager.createDirectoryAtURL(filesDir, true, null, null)
                    }

                    val success = NativeZipSupport.extractArchiveToDirectory(tempZipURL, filesDir)
                    if (!success) {
                        throw Exception("Archive extraction failed")
                    }

                    // Clean up temp file
                    fileManager.removeItemAtURL(tempZipURL, null)

                } catch (e: Exception) {
                    // Clean up temp file on error
                    if (fileManager.fileExistsAtPath(tempZipURL.path ?: "")) {
                        fileManager.removeItemAtURL(tempZipURL, null)
                    }
                    throw e
                }
            }
            else -> throw IllegalArgumentException("Unsupported input stream type: ${zipInputStream::class}")
        }
    }

    override fun createTimestampedFileName(baseName: String, extension: String): String {
        val df = NSDateFormatter().apply { dateFormat = "yyyyMMdd_HHmmss" }
        return "${baseName}_${df.stringFromDate(NSDate())}.$extension"
    }

    override suspend fun listFilesRecursively(path: String): List<String> {
        val options: platform.Foundation.NSDirectoryEnumerationOptions = 0u.toULong()
        val urls = fileManager.contentsOfDirectoryAtURL(getURL(path), null, options, null) as List<NSURL>?
        return urls?.mapNotNull { it.lastPathComponent } ?: emptyList()
    }

    override suspend fun getFileHash(path: String): String? = null

    override suspend fun getDirectorySize(path: String): Long = 0

    override suspend fun getFileSize(path: String): Long {
        val attrs = fileManager.attributesOfItemAtPath(getURL(path).path!!, null)
        return (attrs?.get(NSFileSize) as? NSNumber)?.longValue ?: 0L
    }

    override fun openFileExternally(path: String) { }

    private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }

    private fun NSData.toByteArray(): ByteArray {
        val size = length.toInt()
        val byteArray = ByteArray(size)
        if (size > 0) {
            byteArray.usePinned { pinned ->
                memcpy(pinned.addressOf(0), bytes, length)
            }
        }
        return byteArray
    }
}

actual typealias FileInputSource = NSInputStream
actual abstract class OutputStream {
    actual abstract fun write(b: Int)
    actual open fun write(b: ByteArray, off: Int, len: Int) {}
    actual open fun flush() {}
    actual open fun close() {}
}
