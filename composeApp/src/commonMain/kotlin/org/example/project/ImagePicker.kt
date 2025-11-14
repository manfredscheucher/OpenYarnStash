package org.example.project

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePickerLauncher(onImagesSelected: (List<ByteArray>) -> Unit): ImagePickerLauncher

expect class ImagePickerLauncher {
    fun launch()
}