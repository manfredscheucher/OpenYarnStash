package org.example.project.pdf

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class PdfThumbnailGeneratorJvm : PdfThumbnailGenerator {
    override suspend fun generateThumbnail(pdf: ByteArray, width: Int, height: Int): ByteArray? {
        return try {
            PDDocument.load(ByteArrayInputStream(pdf)).use { document ->
                if (document.numberOfPages > 0) {
                    val renderer = PDFRenderer(document)
                    val image = renderer.renderImageWithDPI(0, 300f) // Render at high DPI for good quality

                    val scaledImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
                    val g = scaledImage.createGraphics()
                    g.drawImage(image, 0, 0, width, height, null)
                    g.dispose()

                    val baos = ByteArrayOutputStream()
                    ImageIO.write(scaledImage, "png", baos)
                    baos.toByteArray()
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
