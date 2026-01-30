package org.example.project

import platform.Foundation.*
import kotlinx.cinterop.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
class IosFileHandler : FileHandler {
    private val fileManager = NSFileManager.defaultManager
    private val documentsDir = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).first() as NSURL

    private fun getURL(path: String): NSURL = documentsDir.URLByAppendingPathComponent(path)!!

    override suspend fun readText(path: String): String {
        val url = getURL(path)
        if (!fileManager.fileExistsAtPath(url.path ?: "")) {
            return ""
        }
        return NSString.stringWithContentsOfURL(url, NSUTF8StringEncoding, null) as String? ?: ""
    }

    override suspend fun writeText(path: String, content: String) {
        (content as NSString).writeToURL(getURL(path), true, NSUTF8StringEncoding, null)
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
        bytes.toNSData().writeToURL(getURL(path), true)
    }

    override suspend fun readBytes(path: String): ByteArray? {
        return NSData.dataWithContentsOfURL(getURL(path))?.toByteArray()
    }

    override fun openInputStream(path: String): FileInputSource? = null

    override suspend fun deleteFile(path: String) {
        fileManager.removeItemAtURL(getURL(path), null)
    }

    override suspend fun zipFiles(): ByteArray = ByteArray(0)

    override suspend fun renameFilesDirectory(newName: String) { }

    override suspend fun restoreBackupDirectory(backupName: String) { }

    override suspend fun deleteFilesDirectory() { }

    override suspend fun unzipAndReplaceFiles(zipInputStream: Any) { }

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

actual class FileInputSource
actual abstract class OutputStream {
    actual abstract fun write(b: Int)
    actual open fun write(b: ByteArray, off: Int, len: Int) {}
    actual open fun flush() {}
    actual open fun close() {}
}
