package org.example.project

import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

actual class FileDownloader {
    actual fun download(fileName: String, data: ByteArray, context: Any?) {
        SwingUtilities.invokeLater {
            val chooser = JFileChooser()
            chooser.dialogTitle = "Save ZIP File"
            chooser.selectedFile = File(fileName)
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                chooser.selectedFile.writeBytes(data)
            }
        }
    }
}
