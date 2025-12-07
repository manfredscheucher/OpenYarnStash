@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLVideoElement
import org.w3c.files.FileReader

actual class CameraLauncher(
    private val onResult: (ByteArray?) -> Unit
) {
    actual fun launch() {
        // In browsers, we use a file input with camera capture
        // This is more compatible than getUserMedia which requires HTTPS
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/*"
        input.setAttribute("capture", "environment") // Use back camera on mobile

        input.onchange = { event ->
            val files = input.files
            if (files != null && files.length > 0) {
                val file = files.item(0)
                if (file != null) {
                    val reader = FileReader()
                    reader.onload = { loadEvent ->
                        val arrayBuffer = reader.result as ArrayBuffer
                        val int8Array = Int8Array(arrayBuffer)
                        val byteArray = ByteArray(int8Array.length) { index ->
                            int8Array.asDynamic()[index] as Byte
                        }
                        onResult(byteArray)
                        null
                    }
                    reader.onerror = {
                        console.error("Failed to read camera image")
                        onResult(null)
                        null
                    }
                    reader.readAsArrayBuffer(file.unsafeCast<org.w3c.files.Blob>())
                } else {
                    onResult(null)
                }
            } else {
                onResult(null)
            }
            null
        }

        input.click()
    }
}

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher? {
    return remember { CameraLauncher(onResult) }
}
