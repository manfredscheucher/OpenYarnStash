@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileReader
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array

actual class ImagePickerLauncher(
    private val onImagesSelected: (List<ByteArray>) -> Unit
) {
    actual fun launch() {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/jpeg,image/png,image/jpg"
        input.multiple = true

        input.onchange = { event ->
            val files = input.files
            if (files != null && files.length > 0) {
                GlobalScope.launch {
                    val imageBytes = mutableListOf<ByteArray>()
                    var processedCount = 0
                    val totalFiles = files.length

                    for (i in 0 until files.length) {
                        val file = files.item(i)
                        if (file != null) {
                            val reader = FileReader()
                            reader.onload = { loadEvent ->
                                val arrayBuffer = reader.result as ArrayBuffer
                                val int8Array = Int8Array(arrayBuffer)
                                val byteArray = ByteArray(int8Array.length) { index ->
                                    int8Array.asDynamic()[index] as Byte
                                }

                                GlobalScope.launch {
                                    // Resize the image before adding it
                                    val resizedBytes = resizeImage(byteArray, 400, 400)
                                    imageBytes.add(resizedBytes)
                                    processedCount++

                                    if (processedCount == totalFiles) {
                                        onImagesSelected(imageBytes)
                                    }
                                }
                                null
                            }
                            reader.readAsArrayBuffer(file.unsafeCast<org.w3c.files.Blob>())
                        } else {
                            processedCount++
                        }
                    }
                }
            }
            null
        }

        input.click()
    }
}

@Composable
actual fun rememberImagePickerLauncher(onImagesSelected: (List<ByteArray>) -> Unit): ImagePickerLauncher {
    return remember { ImagePickerLauncher(onImagesSelected) }
}
