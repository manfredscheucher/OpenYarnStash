package org.example.project

import kotlinx.browser.document
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.Image
import org.w3c.files.Blob
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

actual suspend fun resizeImage(bytes: ByteArray, maxWidth: Int, maxHeight: Int): ByteArray {
    return try {
        // Convert ByteArray to Blob
        val uint8Array = Uint8Array(bytes.size)
        bytes.forEachIndexed { index, byte ->
            uint8Array.asDynamic()[index] = byte.toInt() and 0xFF
        }
        val blobOptions = js("{type: 'image/jpeg'}")
        val blob = Blob(arrayOf(uint8Array), blobOptions.unsafeCast<org.w3c.files.BlobPropertyBag>())

        // Load image to get dimensions
        val img = loadImage(blob)
        val width = img.width
        val height = img.height

        // If image is already small enough, return original bytes
        if (width <= maxWidth && height <= maxHeight) {
            return bytes
        }

        // Calculate new dimensions maintaining aspect ratio
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

        // Create canvas and draw resized image
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        canvas.width = finalWidth
        canvas.height = finalHeight

        val ctx = canvas.getContext("2d") as org.w3c.dom.CanvasRenderingContext2D
        ctx.drawImage(img, 0.0, 0.0, finalWidth.toDouble(), finalHeight.toDouble())

        // Convert canvas to blob and then to ByteArray
        val resizedBlob = canvasToBlob(canvas, "image/jpeg", 0.9)
        blobToByteArray(resizedBlob)
    } catch (e: Exception) {
        console.error("Error resizing image: ${e.message}")
        bytes  // Return original on error
    }
}

private suspend fun loadImage(blob: Blob): HTMLImageElement = suspendCoroutine { cont ->
    val img = Image()
    val url = org.w3c.dom.url.URL.createObjectURL(blob)

    img.onload = {
        org.w3c.dom.url.URL.revokeObjectURL(url)
        cont.resume(img as HTMLImageElement)
        null
    }

    img.onerror = { _, _, _, _, _ ->
        org.w3c.dom.url.URL.revokeObjectURL(url)
        cont.resumeWithException(Exception("Failed to load image"))
        null
    }

    img.src = url
}

private suspend fun canvasToBlob(canvas: HTMLCanvasElement, mimeType: String, quality: Double): Blob = suspendCoroutine { cont ->
    canvas.toBlob({ blob ->
        if (blob != null) {
            cont.resume(blob)
        } else {
            cont.resumeWithException(Exception("Failed to convert canvas to blob"))
        }
    }, mimeType, quality)
}

private suspend fun blobToByteArray(blob: Blob): ByteArray = suspendCoroutine { cont ->
    val reader = FileReader()

    reader.onload = {
        val arrayBuffer = reader.result as ArrayBuffer
        val int8Array = Int8Array(arrayBuffer)
        val byteArray = ByteArray(int8Array.length) { i ->
            int8Array.asDynamic()[i] as Byte
        }
        cont.resume(byteArray)
        null
    }

    reader.onerror = {
        cont.resumeWithException(Exception("Failed to read blob"))
        null
    }

    reader.readAsArrayBuffer(blob)
}
