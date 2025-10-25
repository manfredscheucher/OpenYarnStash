package org.example.project

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

actual fun resizeImage(bytes: ByteArray, maxWidth: Int, maxHeight: Int): ByteArray {
    val originalImage = ImageIO.read(ByteArrayInputStream(bytes))
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

    val resizedImage = BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_RGB)
    val graphics = resizedImage.createGraphics()
    graphics.drawImage(originalImage.getScaledInstance(finalWidth, finalHeight, Image.SCALE_SMOOTH), 0, 0, null)
    graphics.dispose()

    val outputStream = ByteArrayOutputStream()
    ImageIO.write(resizedImage, "jpg", outputStream)
    return outputStream.toByteArray()
}
