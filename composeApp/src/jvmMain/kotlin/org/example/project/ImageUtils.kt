package org.example.project

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

actual fun createEmptyImageByteArray(): ByteArray {
    val image = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
    val baos = ByteArrayOutputStream()
    ImageIO.write(image, "png", baos)
    return baos.toByteArray()
}
