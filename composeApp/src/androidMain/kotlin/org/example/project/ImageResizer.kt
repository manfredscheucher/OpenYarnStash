@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
package org.example.project

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

actual suspend fun resizeImage(bytes: ByteArray, maxWidth: Int, maxHeight: Int): ByteArray {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

    val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val width = originalBitmap.width
    val height = originalBitmap.height

    if (width <= maxWidth && height <= maxHeight) {
        return bytes
    }

    val ratio: Float = width.toFloat() / height.toFloat()

    val finalWidth: Int
    val finalHeight: Int

    if (ratio > 1) {
        finalWidth = maxWidth
        finalHeight = (finalWidth / ratio).toInt()
    } else {
        finalHeight = maxHeight
        finalWidth = (finalHeight * ratio).toInt()
    }

    val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, true)

    val outputStream = ByteArrayOutputStream()
    val format = when (options.outMimeType) {
        "image/png" -> Bitmap.CompressFormat.PNG
        else -> Bitmap.CompressFormat.JPEG
    }
    resizedBitmap.compress(format, 90, outputStream)
    return outputStream.toByteArray()
}
