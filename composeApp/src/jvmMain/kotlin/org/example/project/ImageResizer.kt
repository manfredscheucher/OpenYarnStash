@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
package org.example.project

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import javax.imageio.stream.MemoryCacheImageInputStream

actual fun resizeImage(bytes: ByteArray, maxWidth: Int, maxHeight: Int): ByteArray {
    val bais = ByteArrayInputStream(bytes)
    val originalImage = ImageIO.read(bais)
    val width = originalImage.width
    val height = originalImage.height

    if (width <= maxWidth && height <= maxHeight) {
        return bytes
    }

    // Determine image format
    val iis = MemoryCacheImageInputStream(ByteArrayInputStream(bytes))
    val readers = ImageIO.getImageReaders(iis)
    val formatName = if (readers.hasNext()) {
        val reader = readers.next()
        reader.formatName
    } else {
        // default to jpg if format can't be determined
        "jpg"
    }
    iis.close()


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

    // For PNG, we need to preserve transparency
    val imageType = if (formatName.equals("png", ignoreCase = true)) {
        BufferedImage.TYPE_INT_ARGB
    } else {
        BufferedImage.TYPE_INT_RGB
    }

    val resizedImage = BufferedImage(finalWidth, finalHeight, imageType)
    val graphics = resizedImage.createGraphics()
    graphics.drawImage(originalImage.getScaledInstance(finalWidth, finalHeight, Image.SCALE_SMOOTH), 0, 0, null)
    graphics.dispose()

    val outputStream = ByteArrayOutputStream()
    // Use the original format. ImageIO.write returns false if the format is not supported.
    if (!ImageIO.write(resizedImage, formatName, outputStream)) {
        // fallback to jpg if the original format is not writable.
        outputStream.reset()
        ImageIO.write(resizedImage, "jpg", outputStream)
    }
    return outputStream.toByteArray()
}
