package org.example.project.pdf

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class ProjectPdfExporterJvm(
    private val defaultIconBytes: ByteArray? = null
) : ProjectPdfExporter {
    override suspend fun exportToPdf(project: Project, params: Params, yarns: List<YarnUsage>): ByteArray {
        val baos = ByteArrayOutputStream()
        PDDocument().use { d ->
            val page = PDPage(PDRectangle.A4)
            d.addPage(page)
            PDPageContentStream(d, page).use { cs ->
                var y = PDRectangle.A4.height - 36f

                fun chooseBytes(primary: ByteArray?): ByteArray? = primary ?: defaultIconBytes

                fun createImage(document: PDDocument, bytes: ByteArray): PDImageXObject {
                    // Format über Header erkennen
                    return if (bytes.size > 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()) {
                        // JPEG
                        JPEGFactory.createFromByteArray(document, bytes)
                    } else if (bytes.size > 8 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() /* 'P' */) {
                        // PNG
                        LosslessFactory.createFromImage(document, ImageIO.read(ByteArrayInputStream(bytes)))
                    } else {
                        // Fallback – lässt mehrere Formate zu
                        PDImageXObject.createFromByteArray(document, bytes, "img")
                    }
                }

                fun text(t: String, size: Float, bold: Boolean = false) {
                    cs.beginText()
                    cs.setFont(if (bold) PDType1Font.HELVETICA_BOLD else PDType1Font.HELVETICA, size)
                    cs.newLineAtOffset(36f, y)
                    cs.showText(t)
                    cs.endText()
                    y -= size + 8f
                }

                text(project.title, 18f, true)

                chooseBytes(project.imageBytes)?.let { pdImg ->
                    val img = createImage(d, pdImg)
                    val w = 240f; val ratio = w / img.width; val h = img.height * ratio
                    cs.drawImage(img, 36f, y - h, w, h); y -= h + 12f
                }

                text("Parameter", 12f, true)

                fun param(label: String, value: String?) {
                    if (!value.isNullOrBlank()) {
                        cs.beginText()
                        cs.setFont(PDType1Font.HELVETICA_BOLD, 10f)
                        cs.newLineAtOffset(36f, y)
                        cs.showText(label)
                        cs.endText()
                        cs.beginText()
                        cs.setFont(PDType1Font.HELVETICA, 10f)
                        cs.newLineAtOffset(156f, y)
                        cs.showText(value)
                        cs.endText()
                        y -= 14f
                    }
                }

                param("Maschenprobe:", params.gauge)
                param("Nadeln:", params.needles)
                param("Größe:", params.size)
                param("Gewichtsklasse:", params.yarnWeight)
                params.notes?.let { param("Notizen:", it) }
                y -= 10f

                text("Verwendete Wolle", 12f, true)

                yarns.forEach { usage ->
                    val rowStartY = y
                    chooseBytes(usage.yarn.imageBytes)?.let { ib ->
                        val img = createImage(d, ib)
                        cs.drawImage(img, 36f, rowStartY - 72f, 72f, 72f)
                    }

                    var textY = rowStartY
                    val textX = 36f + 72f + 12f

                    fun line(s: String, size: Float = 10f) {
                        cs.beginText()
                        cs.setFont(PDType1Font.HELVETICA, size)
                        cs.newLineAtOffset(textX, textY - size)
                        cs.showText(s)
                        cs.endText()
                        textY -= size + 4f
                    }

                    listOfNotNull(usage.yarn.brand, usage.yarn.name).joinToString(" · ").takeIf { it.isNotBlank() }?.let { line(it) }
                    listOfNotNull(usage.yarn.colorway, usage.yarn.lot).joinToString(" · ").takeIf { it.isNotBlank() }?.let { line(it, 9f) }
                    listOfNotNull(usage.yarn.material, usage.yarn.weightClass).joinToString(" · ").takeIf { it.isNotBlank() }?.let { line(it, 9f) }
                    buildString {
                        usage.gramsUsed?.let { append("${it.toInt()} g  ") }
                        usage.metersUsed?.let { append("(${it.toInt()} m)") }
                    }.takeIf { it.isNotBlank() }?.let { line(it, 9f) }

                    y = (rowStartY - 72f).coerceAtMost(textY) - 8f
                }
            }
            d.save(baos)
        }
        return baos.toByteArray()
    }
}
