package org.example.project

import androidx.compose.runtime.Composable
import java.awt.FileDialog
import java.awt.Frame

@Composable
actual fun FilePicker(show: Boolean, onFileSelected: (String?) -> Unit) {
    if (show) {
        val dialog = FileDialog(null as Frame?, "Select File", FileDialog.LOAD)
        dialog.isVisible = true
        val file = dialog.file
        val dir = dialog.directory
        if (file != null && dir != null) {
            onFileSelected(java.io.File(dir, file).readText())
        } else {
            onFileSelected(null)
        }
    }
}
