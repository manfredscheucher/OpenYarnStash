package org.example.project

import java.awt.Desktop
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipOutputStream

class JvmFileHandler : BaseJvmAndroidFileHandler() {

    private var baseDir: File
    override val filesDir: File

    init {
        val home = System.getProperty("user.home")
        baseDir = File(home, ".openyarnstash")
        filesDir = File(baseDir, "files")
        if (!filesDir.exists()) {
            filesDir.mkdirs()
        }
    }

    override fun openFileExternally(path: String) {
        val file = getFile(path)
        if (file.exists() && Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file)
        }
    }
}
