package org.example.project

import androidx.compose.runtime.Composable
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun FilePicker(show: Boolean, onFileSelected: (String?) -> Unit) {
    if (show) {
        val fileChooser = JFileChooser()
        fileChooser.fileFilter = FileNameExtensionFilter("JSON Files", "json")
        val result = fileChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFile = fileChooser.selectedFile
            onFileSelected(selectedFile.readText())
        } else {
            onFileSelected(null)
        }
    }
}

@Composable
actual fun FilePickerForZip(show: Boolean, onFileSelected: (Any?) -> Unit) {
    if (show) {
        val fileChooser = JFileChooser()
        fileChooser.fileFilter = FileNameExtensionFilter("ZIP Archives", "zip")
        val result = fileChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFile: File = fileChooser.selectedFile
            onFileSelected(selectedFile.inputStream())
        } else {
            onFileSelected(null)
        }
    }
}
