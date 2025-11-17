package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.FileDialog
import java.awt.Frame
import java.io.ByteArrayOutputStream
import java.io.FilenameFilter
import javax.imageio.ImageIO

actual class ImagePickerLauncher(
    private val onImagesSelected: (List<ByteArray>) -> Unit
) {
    actual fun launch() {
        val fileDialog = FileDialog(null as Frame?, "Select Image", FileDialog.LOAD)
        fileDialog.isMultipleMode = true
        fileDialog.filenameFilter = FilenameFilter { _, name ->
            val lowerCaseName = name.lowercase()
            lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg") || lowerCaseName.endsWith(".png")
        }
        fileDialog.isVisible = true
        val files = fileDialog.files
        if (files != null && files.isNotEmpty()) {
            val imageBytes = files.mapNotNull { file ->
                val image = ImageIO.read(file)
                if (image != null) {
                    val baos = ByteArrayOutputStream()
                    var formatName = file.extension.lowercase()
                    if (formatName == "jpeg") {
                        formatName = "jpg"
                    }
                    if (formatName == "jpg" || formatName == "png") {
                        ImageIO.write(image, formatName, baos)
                        resizeImage(baos.toByteArray(), 400, 400)
                    } else {
                        // Fallback to jpg
                        ImageIO.write(image, "jpg", baos)
                        resizeImage(baos.toByteArray(), 400, 400)
                    }
                } else {
                    null
                }
            }
            onImagesSelected(imageBytes)
        }
    }
}

@Composable
actual fun rememberImagePickerLauncher(onImagesSelected: (List<ByteArray>) -> Unit): ImagePickerLauncher {
    return remember { ImagePickerLauncher(onImagesSelected) }
}
