package org.example.project

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class AndroidFileHandler(private val context: Context) : FileHandler {

    companion object {
        // Static reference to last export ZIP file (shared across all instances)
        @Volatile
        private var lastExportZipFile: File? = null

        fun getLastExportZipFile(): File? = lastExportZipFile

        fun setLastExportZipFile(file: File?) {
            lastExportZipFile = file
        }

        fun cleanupExportZip() {
            lastExportZipFile?.delete()
            lastExportZipFile = null
        }
    }

    private val filesDir: File

    init {
        filesDir = File(context.filesDir, "files")
        if (!filesDir.exists()) {
            filesDir.mkdirs()
        }
    }

    private fun getFile(path: String) = File(filesDir, path)

    override fun openInputStream(path: String): FileInputSource? {
        val file = getFile(path)
        return if (file.exists()) {
            FileInputStream(file)
        } else {
            null
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
        val file = getFile(path)
        if (file.exists()) {
            val backupFileName = createTimestampedFileName(file.nameWithoutExtension, file.extension)
            val backupFile = File(file.parent, backupFileName)
            file.copyTo(backupFile, overwrite = true)
            return backupFile.name
        }
        return null
    }

    override fun createTimestampedFileName(baseName: String, extension: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date())
        return "$baseName-$timestamp.$extension"
    }

    override suspend fun writeBytes(path: String, bytes: ByteArray) {
        val imageFile = getFile(path)
        imageFile.parentFile?.mkdirs()
        imageFile.writeBytes(bytes)
    }

    override suspend fun readBytes(path: String): ByteArray? {
        val imageFile = getFile(path)
        return if (imageFile.exists()) {
            imageFile.readBytes()
        } else {
            null
        }
    }

    override suspend fun deleteFile(path: String) {
        val fileToDelete = getFile(path)
        if (fileToDelete.exists()) {
            fileToDelete.delete()
        }
    }

    override suspend fun zipFiles(): ByteArray {
        // Clean up old export file if exists
        cleanupExportZip()

        // Create ZIP with proper timestamped filename directly
        val exportFileName = createTimestampedFileName("openyarnstash", "zip")
        val zipFile = File(context.cacheDir, exportFileName)

        FileOutputStream(zipFile).use { fos ->
            ZipOutputStream(fos).use { zos ->
                addFolderToZip(filesDir, zos)
            }
        }

        // Store file reference in companion object (static, shared across instances)
        setLastExportZipFile(zipFile)

        // Return empty ByteArray as signal that file is ready (avoids OOM for large files)
        return ByteArray(0)
    }

    private fun addFolderToZip(folder: File, zos: ZipOutputStream) {
        println("[DEBUG] zip: add folder $folder")
        folder.listFiles()?.forEach { file ->
            val relativePath = filesDir.toURI().relativize(file.toURI()).path

            if (file.isDirectory) {
                // Add directory entry (must end with /)
                val dirEntry = ZipEntry(relativePath + "/")
                zos.putNextEntry(dirEntry)
                zos.closeEntry()
                println("[DEBUG] zip: added directory $relativePath/")

                // Recursively add contents
                addFolderToZip(file, zos)
            } else {
                println("[DEBUG] zip: add file $relativePath")
                FileInputStream(file).use { fis ->
                    val entry = ZipEntry(relativePath)
                    zos.putNextEntry(entry)
                    fis.copyTo(zos)
                    zos.closeEntry()
                }
            }
        }
    }


    override suspend fun renameFilesDirectory(newName: String) {
        val newDir = File(filesDir.parentFile, newName)
        if (filesDir.renameTo(newDir)) {
            Logger.log(LogLevel.INFO, "Renamed files directory to: $newName")
        } else {
            Logger.log(LogLevel.ERROR, "Failed to rename files directory to: $newName")
        }
    }

    override suspend fun restoreBackupDirectory(backupName: String) {
        val backupDir = File(filesDir.parentFile, backupName)
        if (backupDir.exists() && backupDir.renameTo(filesDir)) {
            Logger.log(LogLevel.INFO, "Restored backup directory from: $backupName")
        } else {
            Logger.log(LogLevel.ERROR, "Failed to restore backup directory from: $backupName (exists: ${backupDir.exists()})")
        }
    }

    override suspend fun deleteFilesDirectory() {
        if (filesDir.exists()) {
            filesDir.deleteRecursively()
            Logger.log(LogLevel.INFO, "Deleted files directory")
        } else {
            Logger.log(LogLevel.DEBUG, "Files directory does not exist, nothing to delete")
        }
    }

    override suspend fun deleteBackupDirectory(backupName: String) {
        val backupDir = File(filesDir.parentFile, backupName)
        if (backupDir.exists()) {
            backupDir.deleteRecursively()
            Logger.log(LogLevel.INFO, "Deleted backup directory: $backupName")
        } else {
            Logger.log(LogLevel.DEBUG, "Backup directory does not exist: $backupName")
        }
    }

    override suspend fun unzipAndReplaceFiles(zipInputStream: Any) {
        val inputStream = zipInputStream as? InputStream ?: return

        filesDir.mkdirs()

        ZipInputStream(inputStream).use { zis ->
            var entry: ZipEntry?
            while (zis.nextEntry.also { entry = it } != null) {
                val entryFile = File(filesDir, entry!!.name)
                if (entry!!.isDirectory) {
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

    override fun openFileExternally(path: String) {
        val file = getFile(path)
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override suspend fun listFilesRecursively(path: String): List<String> {
        val rootDir = getFile(path)
        if (!rootDir.exists() || !rootDir.isDirectory) {
            return emptyList()
        }
        return rootDir.walkTopDown()
            .filter { it.isFile }
            .map { filesDir.toURI().relativize(it.toURI()).path }
            .toList()
    }

    override suspend fun getFileHash(path: String): String? {
        val file = getFile(path)
        if (!file.exists() || !file.isFile) {
            return null
        }
        val md = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead = fis.read(buffer)
            while (bytesRead != -1) {
                md.update(buffer, 0, bytesRead)
                bytesRead = fis.read(buffer)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    override suspend fun getDirectorySize(path: String): Long {
        val directory = getFile(path)
        if (!directory.exists() || !directory.isDirectory) {
            return 0L
        }
        return directory.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    override suspend fun getFileSize(path: String): Long {
        val file = getFile(path)
        return if (file.exists() && file.isFile) {
            file.length()
        } else {
            0L
        }
    }
}
