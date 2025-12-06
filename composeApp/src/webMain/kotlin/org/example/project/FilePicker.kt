package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileReader
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array

@Composable
actual fun FilePicker(show: Boolean, onFileSelected: (String?) -> Unit) {
    LaunchedEffect(show) {
        if (show) {
            // Create a hidden file input element
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = ".json,application/json"
            input.multiple = false

            input.onchange = {
                val files = input.files
                if (files != null && files.length > 0) {
                    val file = files.item(0) as? File
                    if (file != null) {
                        readFileAsText(file) { text ->
                            onFileSelected(text)
                        }
                    } else {
                        onFileSelected(null)
                    }
                } else {
                    onFileSelected(null)
                }
                null
            }

            // Trigger the file picker
            input.click()
        }
    }
}

@Composable
actual fun FilePickerForZip(show: Boolean, onFileSelected: (Any?) -> Unit) {
    LaunchedEffect(show) {
        if (show) {
            // Create a hidden file input element
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = ".zip,application/zip"
            input.multiple = false

            input.onchange = {
                val files = input.files
                if (files != null && files.length > 0) {
                    val file = files.item(0) as? File
                    if (file != null) {
                        readFileAsBytes(file) { byteArray ->
                            onFileSelected(byteArray)
                        }
                    } else {
                        onFileSelected(null)
                    }
                } else {
                    onFileSelected(null)
                }
                null
            }

            // Trigger the file picker
            input.click()
        }
    }
}

private fun readFileAsText(file: File, callback: (String?) -> Unit) {
    val reader = FileReader()

    reader.onload = {
        try {
            val text = reader.result as String
            callback(text)
        } catch (e: Exception) {
            console.error("Error reading JSON file: ${e.message}")
            callback(null)
        }
        null
    }

    reader.onerror = {
        console.error("Failed to read file")
        callback(null)
        null
    }

    reader.readAsText(file)
}

private fun readFileAsBytes(file: File, callback: (ByteArray?) -> Unit) {
    val reader = FileReader()

    reader.onload = {
        try {
            val arrayBuffer = reader.result as ArrayBuffer
            val int8Array = Int8Array(arrayBuffer)
            val byteArray = ByteArray(int8Array.length) { i ->
                int8Array.asDynamic()[i] as Byte
            }
            callback(byteArray)
        } catch (e: Exception) {
            console.error("Error reading file: ${e.message}")
            callback(null)
        }
        null
    }

    reader.onerror = {
        console.error("Failed to read file")
        callback(null)
        null
    }

    reader.readAsArrayBuffer(file)
}
