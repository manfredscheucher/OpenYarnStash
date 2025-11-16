package org.example.project

import java.awt.Desktop
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class JvmFileHandler : FileHandler {

    private var baseDir: File
    private val filesDir: File

    init {
        val home = System.getProperty("user.home")
        baseDir = File(home, ".openyarnstash")
        filesDir = File(baseDir, "files")
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

    override suspend fun readFile(path: String): String {
        val file = getFile(path)
        return if (file.exists()) {
            file.readText()
        } else {
            ""
        }
    }

    override suspend fun writeFile(path: String, content: String) {
        val file = getFile(path)
        file.parentFile?.mkdirs()
        file.writeText(content)
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
        throw UnsupportedOperationException("zipFiles is not supported on JVM")
    }

    override suspend fun renameFilesDirectory(newName: String) {
        val newDir = File(baseDir, newName)
        if (filesDir.renameTo(newDir)) {
            // No need to update baseDir, as it's the parent of filesDir
        }
    }

    override suspend fun unzipAndReplaceFiles(zipInputStream: Any) {
        val inputStream = zipInputStream as? InputStream ?: return

        if (filesDir.exists()) {
            val backupDirName = "files-${SimpleDateFormat("yyyyMMdd-HHmmss").format(Date())}"
            renameFilesDirectory(backupDirName)
        }
        filesDir.mkdirs()

        ZipInputStream(inputStream).use { zis ->
            var ze: ZipEntry?
            while (zis.nextEntry.also { ze = it } != null) {
                val entry = ze ?: continue
                val entryFile = File(filesDir, entry.name)
                if (entry.isDirectory) {
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
        if (file.exists() && Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file)
        }
    }
}