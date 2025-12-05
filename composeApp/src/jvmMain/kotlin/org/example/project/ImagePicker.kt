@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
            GlobalScope.launch {
                val imageBytes = files.mapNotNull { file ->
                    val image = ImageIO.read(file)
                    if (image != null) {
                        val baos = ByteArrayOutputStream()
                        val formatName = file.extension.lowercase().let {
                            if (it == "jpeg") "jpg" else it
                        }
                        
                        if (formatName == "jpg" || formatName == "png") {
                            ImageIO.write(image, formatName, baos)
                            resizeImage(baos.toByteArray(), 400, 400)
                        } else {
                            // Unsupported format, do not process
                            null
                        }
                    } else {
                        null
                    }
                }
                onImagesSelected(imageBytes)
            }
        }
    }
}

@Composable
actual fun rememberImagePickerLauncher(onImagesSelected: (List<ByteArray>) -> Unit): ImagePickerLauncher {
    return remember { ImagePickerLauncher(onImagesSelected) }
}
