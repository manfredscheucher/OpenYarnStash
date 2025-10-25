package org.example.project

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePickerLauncher(onImageSelected: (ByteArray) -> Unit): ImagePickerLauncher

expect class ImagePickerLauncher {
    fun launch()
}