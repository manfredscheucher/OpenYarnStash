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

@Composable
actual fun rememberImagePickerLauncher(onImagesSelected: (List<ByteArray>) -> Unit): ImagePickerLauncher {
    return remember { ImagePickerLauncher(onImagesSelected) }
}

actual class ImagePickerLauncher(private val onImagesSelected: (List<ByteArray>) -> Unit) {
    actual fun launch() {
        // Create a hidden file input element
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/*"
        input.multiple = true

        input.onchange = {
            val files = input.files
            if (files != null && files.length > 0) {
                GlobalScope.launch {
                    val imageByteArrays = mutableListOf<ByteArray>()

                    for (i in 0 until files.length) {
                        val file = files.item(i) as? File ?: continue

                        try {
                            val byteArray = readFileAsBytes(file)
                            // Resize image to 400x400
                            val resizedBytes = resizeImage(byteArray, 400, 400)
                            imageByteArrays.add(resizedBytes)
                        } catch (e: Exception) {
                            console.error("Error processing image file: ${e.message}")
                        }
                    }

                    onImagesSelected(imageByteArrays)
                }
            } else {
                onImagesSelected(emptyList())
            }
            null
        }

        // Trigger the file picker
        input.click()
    }

    private suspend fun readFileAsBytes(file: File): ByteArray {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            val reader = FileReader()

            reader.onload = {
                try {
                    val arrayBuffer = reader.result as ArrayBuffer
                    val int8Array = Int8Array(arrayBuffer)
                    val byteArray = ByteArray(int8Array.length) { i ->
                        int8Array.asDynamic()[i] as Byte
                    }
                    continuation.resumeWith(Result.success(byteArray))
                } catch (e: Exception) {
                    console.error("Error reading image file: ${e.message}")
                    continuation.resumeWith(Result.failure(e))
                }
                null
            }

            reader.onerror = {
                continuation.resumeWith(Result.failure(Exception("Failed to read file")))
                null
            }

            reader.readAsArrayBuffer(file)
        }
    }
}
