package org.example.project

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    try {
        val skiaImage = Image.makeFromEncoded(this)
        if (skiaImage == null) {
            throw IllegalArgumentException("Failed to decode image")
        }
        return skiaImage.toComposeImageBitmap()
    } catch (e: Exception) {
        console.error("Failed to decode image: ${e.message}")
        throw e
    }
}