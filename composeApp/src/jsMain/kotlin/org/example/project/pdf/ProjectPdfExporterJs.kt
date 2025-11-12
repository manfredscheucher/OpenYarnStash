package org.example.project.pdf

import org.example.project.ImageManager
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get

@JsModule("jspdf")
@JsNonModule
private external class jsPDF {
    fun addImage(dataUrl: String, format: String, x: Number, y: Number, w: Number, h: Number)
    fun text(text: String, x: Number, y: Number)
    fun output(type: String = definedExternally): dynamic
}

class ProjectPdfExporterJs : ProjectPdfExporter {
    override suspend fun exportToPdf(project: Project, params: Params, yarns: List<YarnUsage>, imageManager: ImageManager): ByteArray {
        val pdf = js("new jspdf.jsPDF()") as jsPDF
        var y = 20
        pdf.text(project.title, 20, y); y += 12

        project.imageIds.firstOrNull()?.let { imageId ->
            imageManager.getProjectImage(project.id, imageId)?.let { bytes ->
                val dataUrl = "data:image/jpeg;base64,${bytes.encodeBase64()}"
                pdf.addImage(dataUrl, "JPEG", 20, y, 120, 90)
                y += 100
            }
        }

        pdf.text("Parameter", 20, y); y += 10
        fun param(label: String, value: String?) {
            if (!value.isNullOrBlank()) {
                pdf.text("$label $value", 20, y); y += 8
            }
        }
        param("Maschenprobe:", params.gauge)
        param("Nadeln:", params.needles)
        param("Größe:", params.size)
        param("Gewichtsklasse:", params.yarnWeight)
        params.notes?.let { param("Notizen:", it) }
        y += 6
        pdf.text("Verwendete Wolle", 20, y); y += 10
        yarns.forEach { usage ->
            val rowY = y
            usage.yarn.imageIds.firstOrNull()?.let { imageId ->
                imageManager.getYarnImage(usage.yarn.id, imageId)?.let { b ->
                    pdf.addImage("data:image/jpeg;base64,${b.encodeBase64()}", "JPEG", 20, y, 36, 36)
                }
            }
            val x = 20 + 36 + 8
            var textY = rowY + 5 // Adjust for vertical alignment
            listOfNotNull(usage.yarn.brand, usage.yarn.name).joinToString(" · ").takeIf { it.isNotBlank() }?.let {
                pdf.text(it, x, textY); textY += 8
            }
            // Add other yarn details as needed
            y = rowY + 36 + 10 // Move to next item
        }
        val u8: dynamic = pdf.output("arraybuffer");
        return (u8.unsafeCast<Int8Array>()).toByteArray()
    }
}

private fun Int8Array.toByteArray(): ByteArray {
    return ByteArray(this.length) { this[it] }
}

@JsName("btoa")
private external fun btoa(data: String): String

private fun ByteArray.encodeBase64(): String {
    var str = ""
    this.forEach { str += it.toInt().toChar() }
    return btoa(str)
}
