package org.example.project.pdf

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.example.project.ImageManager
import java.io.ByteArrayOutputStream

class ProjectPdfExporterJvm : ProjectPdfExporter {
    override suspend fun exportToPdf(project: Project, params: Params, yarns: List<YarnUsage>, imageManager: ImageManager): ByteArray {
        val baos = ByteArrayOutputStream()
        PDDocument().use { d ->
            val page = PDPage(PDRectangle.A4)
            d.addPage(page)
            PDPageContentStream(d, page).use { cs ->
                var y = PDRectangle.A4.height - 36f

                fun text(t: String, size: Float, bold: Boolean = false) {
                    cs.beginText()
                    cs.setFont(PDType1Font(if (bold) Standard14Fonts.FontName.HELVETICA_BOLD else Standard14Fonts.FontName.HELVETICA), size)
                    cs.newLineAtOffset(36f, y)
                    cs.showText(t)
                    cs.endText()
                    y -= size + 8f
                }

                text(project.title, 18f, true)

                project.imageIds.firstOrNull()?.let { imageId ->
                    imageManager.getProjectImageInputStream(project.id, imageId)?.let { stream ->
                        val pdImg = JPEGFactory.createFromStream(d, stream)
                        val w = 240f
                        val ratio = w / pdImg.width
                        val h = pdImg.height * ratio
                        cs.drawImage(pdImg, 36f, y - h, w, h)
                        y -= h + 12f
                    }
                }

                text("Parameter", 12f, true)

                fun param(label: String, value: String?) {
                    if (!value.isNullOrBlank()) {
                        cs.beginText()
                        cs.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10f)
                        cs.newLineAtOffset(36f, y)
                        cs.showText(label)
                        cs.endText()
                        cs.beginText()
                        cs.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 10f)
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
                    usage.yarn.imageIds.firstOrNull()?.let { imageId ->
                        imageManager.getYarnImageInputStream(usage.yarn.id, imageId)?.let { stream ->
                            val pdImg = JPEGFactory.createFromStream(d, stream)
                            cs.drawImage(pdImg, 36f, rowStartY - 72f, 72f, 72f)
                        }
                    }

                    var textY = rowStartY
                    val textX = 36f + 72f + 12f

                    fun line(s: String, size: Float = 10f) {
                        cs.beginText()
                        cs.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), size)
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
