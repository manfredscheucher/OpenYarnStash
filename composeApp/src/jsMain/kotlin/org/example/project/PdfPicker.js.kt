package org.example.project

import androidx.compose.runtime.Composable
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader

@Composable
actual fun rememberPdfPickerLauncher(onPdfSelected: (ByteArray?) -> Unit): (String) -> Unit {
    return { mimeType ->
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = ".pdf,application/pdf"
        input.onchange = {
            val file = input.files?.item(0)
            if (file != null) {
                val reader = FileReader()
                reader.onload = {
                    val arrayBuffer = reader.result as ArrayBuffer
                    val int8Array = Int8Array(arrayBuffer)
                    val byteArray = ByteArray(int8Array.length) { index ->
                        int8Array.asDynamic()[index] as Byte
                    }
                    onPdfSelected(byteArray)
                    null
                }
                reader.onerror = {
                    console.error("Failed to read PDF file")
                    onPdfSelected(null)
                    null
                }
                reader.readAsArrayBuffer(file.unsafeCast<org.w3c.files.Blob>())
            } else {
                onPdfSelected(null)
            }
            null
        }
        input.click()
    }
}
