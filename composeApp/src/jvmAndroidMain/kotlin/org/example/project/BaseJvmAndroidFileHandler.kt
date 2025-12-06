package org.example.project

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Base class for JVM and Android FileHandler implementations.
 * Contains shared logic for file operations that work identically on both platforms.
 */
abstract class BaseJvmAndroidFileHandler : FileHandler {

    protected abstract val filesDir: File

    protected fun getFile(path: String) = File(filesDir, path)


    override suspend fun zipFiles(): ByteArray {
        Logger.log(LogLevel.INFO, "zipFiles started")
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            addFolderToZip(filesDir, filesDir, zos)
        }
        Logger.log(LogLevel.INFO,"zipFiles complete")
        return baos.toByteArray()
    }

    protected fun openFileInputStream(path: String): FileInputStream? {
        val file = getFile(path)
        return if (file.exists()) {
            FileInputStream(file)
        } else {
            null
        }
    }

    override fun openInputStream(path: String): FileInputSource? {
        return openFileInputStream(path) as FileInputSource?
    }

    /**
     * Calculate SHA-256 hash of a file
     */
    protected fun getFileHash(file: File): String? {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get total size of a directory recursively
     */
    protected fun getDirectorySize(directory: File): Long {
        var size = 0L
        if (directory.exists()) {
            directory.walkTopDown().forEach { file ->
                if (file.isFile) {
                    size += file.length()
                }
            }
        }
        return size
    }

    /**
     * Get size of a single file
     */
    protected fun getFileSize(file: File): Long {
        return if (file.exists() && file.isFile) file.length() else 0L
    }

    /**
     * List all files recursively in a directory
     */
    protected fun listFilesRecursively(directory: File): List<String> {
        return if (directory.exists() && directory.isDirectory) {
            directory.walkTopDown()
                .filter { it.isFile }
                .map { it.absolutePath }
                .toList()
        } else {
            emptyList()
        }
    }

    /**
     * Add a folder and its contents to a ZIP archive recursively
     */
    protected fun addFolderToZip(folder: File, rootDir: File, zos: ZipOutputStream) {
        folder.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                addFolderToZip(file, rootDir, zos)
            } else {
                FileInputStream(file).use { fis ->
                    val entry = ZipEntry(rootDir.toURI().relativize(file.toURI()).path)
                    zos.putNextEntry(entry)
                    fis.copyTo(zos)
                    zos.closeEntry()
                }
            }
        }
    }

    /**
     * Delete a directory and all its contents recursively
     */
    protected fun deleteDirectoryRecursively(directory: File): Boolean {
        return try {
            if (directory.exists()) {
                directory.deleteRecursively()
            } else {
                true
            }
        } catch (e: IOException) {
            false
        }
    }

    override suspend fun readText(path: String): String {
        val file = getFile(path)
        return if (file.exists()) {
            file.readText()
        } else {
            ""
        }
    }

    override suspend fun writeText(path: String, content: String) {
        val file = getFile(path)
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    override suspend fun appendText(path: String, content: String) {
        val file = getFile(path)
        file.parentFile?.mkdirs()
        file.appendText(content)
    }

    override suspend fun backupFile(path: String): String? {
        Logger.log(LogLevel.INFO, "backupFile")
        val file = getFile(path)
        if (file.exists()) {
            val backupFileName = createTimestampedFileName(file.nameWithoutExtension, file.extension)
            val backupFile = File(file.parent, backupFileName)
            file.copyTo(backupFile, overwrite = true)
            return backupFile.name
        }
        return null
    }

    override suspend fun writeBytes(path: String, bytes: ByteArray) {
        Logger.log(LogLevel.INFO, "writeBytes")
        val imageFile = getFile(path)
        imageFile.parentFile?.mkdirs()
        imageFile.writeBytes(bytes)
    }

    override suspend fun readBytes(path: String): ByteArray? {
        Logger.log(LogLevel.INFO, "readBytes")
        val imageFile = getFile(path)
        return if (imageFile.exists()) {
            imageFile.readBytes()
        } else {
            null
        }
    }

    override suspend fun deleteFile(path: String) {
        Logger.log(LogLevel.INFO, "deleteFile")
        val fileToDelete = getFile(path)
        if (fileToDelete.exists()) {
            fileToDelete.delete()
        }
    }

    override suspend fun deleteFilesDirectory() {
        Logger.log(LogLevel.INFO, "deleteFilesDirectory")
        deleteDirectoryRecursively(filesDir)
    }

    override suspend fun unzipAndReplaceFiles(zipInputStream: Any) {
        Logger.log(LogLevel.INFO, "unzipAndReplaceFiles")
        val inputStream = zipInputStream as? InputStream ?: return

        filesDir.mkdirs()

        ZipInputStream(inputStream).use { zis ->
            var entry: ZipEntry?
            while (zis.nextEntry.also { entry = it } != null) {
                val zipEntry = entry ?: continue
                val entryFile = File(filesDir, zipEntry.name)
                if (zipEntry.isDirectory) {
                    entryFile.mkdirs()
                } else {
                    entryFile.parentFile?.mkdirs()
                    FileOutputStream(entryFile).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
            }
        }
    }

    override suspend fun listFilesRecursively(path: String): List<String> {
        Logger.log(LogLevel.INFO, "listFilesRecursively")
        val rootDir = getFile(path)
        return listFilesRecursively(rootDir)
            .map { File(it).toURI().path.let { fullPath ->
                filesDir.toURI().relativize(File(fullPath).toURI()).path
            }}
    }

    override suspend fun getFileHash(path: String): String? {
        val file = getFile(path)
        return getFileHash(file)
    }

    override suspend fun getDirectorySize(path: String): Long {
        Logger.log(LogLevel.INFO, "getDirectorySize")
        val directory = getFile(path)
        return getDirectorySize(directory)
    }

    override suspend fun getFileSize(path: String): Long {
        Logger.log(LogLevel.INFO, "getFileSize")
        val file = getFile(path)
        return getFileSize(file)
    }

    override suspend fun renameFilesDirectory(newName: String) {
        Logger.log(LogLevel.INFO, "renameFilesDirectory")
        val newDir = File(filesDir.parentFile, newName)
        filesDir.renameTo(newDir)
    }
}
