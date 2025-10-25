package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.FileDialog
import java.awt.Frame
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

actual class ImagePickerLauncher(
    private val onImageSelected: (ByteArray) -> Unit
) {
    actual fun launch() {
        val fileDialog = FileDialog(null as Frame?, "Select Image", FileDialog.LOAD)
        fileDialog.isVisible = true
        val file = fileDialog.file
        val dir = fileDialog.directory
        if (file != null && dir != null) {
            val image = ImageIO.read(java.io.File(dir, file))
            val baos = ByteArrayOutputStream()
            ImageIO.write(image, "jpg", baos)
            val resizedImage = resizeImage(baos.toByteArray(), 400, 400)
            onImageSelected(resizedImage)
        }
    }
}

@Composable
actual fun rememberImagePickerLauncher(onImageSelected: (ByteArray) -> Unit): ImagePickerLauncher {
    return remember { ImagePickerLauncher(onImageSelected) }
}
