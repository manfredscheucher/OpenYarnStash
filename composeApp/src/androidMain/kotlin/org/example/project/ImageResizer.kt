package org.example.project

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

actual fun resizeImage(bytes: ByteArray, maxWidth: Int, maxHeight: Int): ByteArray {
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
    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
    return outputStream.toByteArray()
}
