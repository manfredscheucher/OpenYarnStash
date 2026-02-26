package org.example.project

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePickerLauncher(onImagesSelected: (List<ByteArray>) -> Unit): ImagePickerLauncher

interface ImagePickerLauncher {
    fun launch()
}