package org.example.project

import java.awt.Desktop
import java.io.ByteArrayOutputStream
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
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            addFolderToZip(filesDir, zos)
        }
        return baos.toByteArray()
    }

    private fun addFolderToZip(folder: File, zos: ZipOutputStream) {
        println("zip: add folder $folder")
        folder.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                addFolderToZip(file, zos)
            } else {
                println("zip: add file $file")
                FileInputStream(file).use { fis ->
                    val entry = ZipEntry(filesDir.toURI().relativize(file.toURI()).path)
                    zos.putNextEntry(entry)
                    fis.copyTo(zos)
                    zos.closeEntry()
                }
            }
        }
    }

    override suspend fun renameFilesDirectory(newName: String) {
        val newDir = File(baseDir, newName)
        if (filesDir.renameTo(newDir)) {
            // No need to update baseDir, as it's the parent of filesDir
        }
    }

    override suspend fun unzipAndReplaceFiles(zipInputStream: Any) {
        val inputStream = zipInputStream as? InputStream ?: return

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
}