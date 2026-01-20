package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberImagePickerLauncher(onImagesSelected: (List<ByteArray>) -> Unit): ImagePickerLauncher {
    return remember { ImagePickerLauncher(onImagesSelected) }
}

actual class ImagePickerLauncher(private val onImagesSelected: (List<ByteArray>) -> Unit) {
    actual fun launch() {
        // Image picker for iOS can be implemented with UIImagePickerController
        // For now, return empty list
        onImagesSelected(emptyList())
    }
}
