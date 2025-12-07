package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileReader

@Composable
actual fun FilePicker(show: Boolean, onFileSelected: (String?) -> Unit) {
    if (show) {
        LaunchedEffect(Unit) {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = ".json"
            input.onchange = {
                val file = input.files?.item(0)
                if (file != null) {
                    val reader = FileReader()
                    reader.onload = {
                        onFileSelected(reader.result as? String)
                        null
                    }
                    reader.readAsText(file)
                } else {
                    onFileSelected(null)
                }
                null
            }
            input.click()
        }
    }
}

@Composable
actual fun FilePickerForZip(show: Boolean, onFileSelected: (Any?) -> Unit) {
    if (show) {
        LaunchedEffect(Unit) {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = ".zip"
            input.onchange = {
                val file = input.files?.item(0)
                if (file != null) {
                    onFileSelected(file)
                } else {
                    onFileSelected(null)
                }
                null
            }
            input.click()
        }
    }
}
