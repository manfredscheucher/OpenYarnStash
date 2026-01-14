package org.example.project

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    return try {
        if (this.isEmpty()) {
            console.error("toImageBitmap called with empty ByteArray")
            ImageBitmap(1, 1)
        } else {
            Image.makeFromEncoded(this).toComposeImageBitmap()
        }
    } catch (e: Exception) {
        console.error("Error in toImageBitmap", e)
        ImageBitmap(1, 1)
    }
}
