package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun FilePicker(show: Boolean, onFileSelected: (String?) -> Unit) {
    // iOS file picker can be implemented with UIDocumentPickerViewController
    // For now, return null
    LaunchedEffect(show) {
        if (show) {
            onFileSelected(null)
        }
    }
}

@Composable
actual fun FilePickerForZip(show: Boolean, onFileSelected: (Any?) -> Unit) {
    // iOS file picker for ZIP can be implemented with UIDocumentPickerViewController
    // For now, return null
    LaunchedEffect(show) {
        if (show) {
            onFileSelected(null)
        }
    }
}
