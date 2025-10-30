package org.example.project.pdf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File
import javax.swing.JFileChooser

@Composable
actual fun rememberPdfSaver(): (fileName: String, data: ByteArray) -> Unit {
    return remember {
        { fileName: String, data: ByteArray ->
            val fileChooser = JFileChooser()
            fileChooser.selectedFile = File(fileName)
            val result = fileChooser.showSaveDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                fileChooser.selectedFile.writeBytes(data)
            }
        }
    }
}
