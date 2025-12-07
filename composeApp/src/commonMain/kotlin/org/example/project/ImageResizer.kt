package org.example.project

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.SamplingMode

suspend fun resizeImage(bytes: ByteArray, maxWidth: Int, maxHeight: Int): ByteArray {
    val originalImage = Image.makeFromEncoded(bytes)
    val width = originalImage.width
    val height = originalImage.height

    if (width <= maxWidth && height <= maxHeight) {
        return bytes
    }

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

    val bitmap = Bitmap()
    bitmap.allocN32Pixels(finalWidth, finalHeight)
    val canvas = Canvas(bitmap)
    canvas.drawImageRect(
        originalImage,
        Rect.makeWH(width.toFloat(), height.toFloat()),
        Rect.makeWH(finalWidth.toFloat(), finalHeight.toFloat()),
        SamplingMode.MITCHELL,
        Paint(),
        false
    )

    val resizedImage = Image.makeFromBitmap(bitmap)

    // A simple check for PNG magic bytes.
    val isPng = bytes.size >= 8 &&
            bytes[0] == 0x89.toByte() &&
            bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x4E.toByte() &&
            bytes[3] == 0x47.toByte() &&
            bytes[4] == 0x0D.toByte() &&
            bytes[5] == 0x0A.toByte() &&
            bytes[6] == 0x1A.toByte() &&
            bytes[7] == 0x0A.toByte()

    val format = if (isPng) EncodedImageFormat.PNG else EncodedImageFormat.JPEG
    val quality = if (isPng) 100 else 90

    return resizedImage.encodeToData(format, quality)!!.bytes
}
