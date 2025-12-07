@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
package org.example.project

import kotlinx.browser.document
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.Image
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

actual suspend fun resizeImage(bytes: ByteArray, maxWidth: Int, maxHeight: Int): ByteArray {
    return suspendCoroutine { continuation ->
        // Create a blob from the byte array
        val uint8Array = Uint8Array(bytes.size)
        for (i in bytes.indices) {
            uint8Array.asDynamic()[i] = bytes[i].toInt() and 0xFF
        }

        val blob = Blob(arrayOf(uint8Array), BlobPropertyBag(type = "image/jpeg"))
        val url = js("URL.createObjectURL(blob)") as String

        // Create an image element
        val img = Image()
        img.onload = {
            val width = img.width
            val height = img.height

            // Check if resizing is needed
            if (width <= maxWidth && height <= maxHeight) {
                continuation.resume(bytes)
            } else {
                // Calculate new dimensions
                val ratio = width.toDouble() / height.toDouble()
                val finalWidth: Int
                val finalHeight: Int

                if (ratio > 1) {
                    finalWidth = maxWidth
                    finalHeight = (finalWidth / ratio).toInt()
                } else {
                    finalHeight = maxHeight
                    finalWidth = (finalHeight * ratio).toInt()
                }

                // Create canvas and resize
                val canvas = document.createElement("canvas") as HTMLCanvasElement
                canvas.width = finalWidth
                canvas.height = finalHeight
                val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
                ctx.drawImage(img, 0.0, 0.0, finalWidth.toDouble(), finalHeight.toDouble())

                // Convert canvas to blob
                canvas.toBlob({ resizedBlob ->
                    if (resizedBlob != null) {
                        val reader = org.w3c.files.FileReader()
                        reader.onload = {
                            val arrayBuffer = reader.result as ArrayBuffer
                            val int8Array = Int8Array(arrayBuffer)
                            val resizedBytes = ByteArray(int8Array.length) { index ->
                                int8Array.asDynamic()[index] as Byte
                            }
                            continuation.resume(resizedBytes)
                            null
                        }
                        reader.readAsArrayBuffer(resizedBlob.unsafeCast<org.w3c.files.Blob>())
                    } else {
                        continuation.resume(bytes)
                    }
                }, "image/jpeg", 0.9)
            }

            // Clean up URL
            js("URL.revokeObjectURL(url)")
        }

        img.onerror = { _, _, _, _, _ ->
            console.error("Failed to load image for resizing")
            continuation.resume(bytes)
            js("URL.revokeObjectURL(url)")
            null
        }

        img.src = url
    }
}
