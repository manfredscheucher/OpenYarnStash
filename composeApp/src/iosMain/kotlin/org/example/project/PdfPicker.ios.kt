package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberPdfPickerLauncher(onPdfSelected: (ByteArray?) -> Unit): (String) -> Unit {
    return remember {
        { mimeType: String ->
            // PDF picker for iOS can be implemented with UIDocumentPickerViewController
            // For now, return null
            onPdfSelected(null)
        }
    }
}
