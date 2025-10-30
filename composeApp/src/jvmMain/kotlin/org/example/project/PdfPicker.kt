package org.example.project

import androidx.compose.runtime.Composable
import java.awt.FileDialog
import java.awt.Frame

@Composable
actual fun rememberPdfPickerLauncher(onPdfSelected: (ByteArray?) -> Unit): (String) -> Unit {
    return { mimeType ->
        val fileDialog = FileDialog(null as Frame?, "Select PDF", FileDialog.LOAD)
        fileDialog.setFilenameFilter { _, name -> name.endsWith(".pdf") }
        fileDialog.isVisible = true
        val file = fileDialog.file
        val dir = fileDialog.directory
        if (file != null && dir != null) {
            val pdfFile = java.io.File(dir, file)
            onPdfSelected(pdfFile.readBytes())
        } else {
            onPdfSelected(null)
        }
    }
}
