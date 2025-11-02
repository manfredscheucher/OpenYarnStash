package org.example.project.pdf

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max

class ProjectPdfExporterAndroid(
    private val defaultIconBytes: ByteArray? = null
) : ProjectPdfExporter {

    companion object {
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 36
        private const val CONTENT_RIGHT = PAGE_WIDTH - MARGIN
        private const val IMAGE_MAX_W = 240
        private const val IMAGE_MAX_H = 180
        private const val ROW_IMAGE_SIZE = 72
        private const val ROW_GAP = 10
        private const val SECTION_GAP = 16
    }

    private fun decodeBitmapOrNull(bytes: ByteArray?): Bitmap? =
        try { bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) } } catch (_: Throwable) { null }

    private fun chooseImageBytes(primary: ByteArray?): ByteArray? =
        primary ?: defaultIconBytes

    override suspend fun exportToPdf(project: Project, params: Params, yarns: List<YarnUsage>): ByteArray = withContext(Dispatchers.Default) {
        val pdf = PdfDocument()
        var pageNumber = 1
        var page = pdf.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); textSize = 20f }
        val h2Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); textSize = 14f }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textSize = 10f; color = Color.DKGRAY }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); textSize = 10f }
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); textSize = 9f; color = Color.DKGRAY }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.LTGRAY; strokeWidth = 1f }

        var currentY = MARGIN
        fun newPage() { pdf.finishPage(page); pageNumber += 1; page = pdf.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()); canvas = page.canvas; currentY = MARGIN }
        fun drawDivider(y: Int) { canvas.drawLine(MARGIN.toFloat(), y.toFloat(), CONTENT_RIGHT.toFloat(), y.toFloat(), linePaint) }
        fun drawTextWrapped(text: String, x: Int, yStart: Int, maxWidth: Int, paint: Paint): Int {
            var y = yStart
            val words = text.split(" ")
            var line = StringBuilder()
            val fm = paint.fontMetrics
            val lineHeight = (fm.bottom - fm.top + fm.leading).toInt()
            for (w in words) {
                val probe = if (line.isEmpty()) w else line.toString() + " " + w
                val wWidth = paint.measureText(probe)
                if (wWidth > maxWidth) { canvas.drawText(line.toString(), x.toFloat(), y.toFloat() - fm.top, paint); y += lineHeight; line = StringBuilder(w) } else { line = StringBuilder(probe) }
            }
            if (line.isNotEmpty()) { canvas.drawText(line.toString(), x.toFloat(), y.toFloat() - fm.top, paint); y += lineHeight }
            return y
        }
        fun ensureSpace(requested: Int) { if (currentY + requested > PAGE_HEIGHT - MARGIN) newPage() }

        // Title
        canvas.drawText(project.title, MARGIN.toFloat(), (currentY - titlePaint.fontMetrics.top), titlePaint)
        currentY += (titlePaint.fontMetrics.bottom - titlePaint.fontMetrics.top).toInt() + SECTION_GAP

        // Project Image (ByteArray)
        val projBmp = decodeBitmapOrNull(chooseImageBytes(project.imageBytes))
        projBmp?.let { bmp ->
            val ratio = minOf(IMAGE_MAX_W.toFloat() / bmp.width, IMAGE_MAX_H.toFloat() / bmp.height)
            val w = (bmp.width * ratio).toInt().coerceAtLeast(1)
            val h = (bmp.height * ratio).toInt().coerceAtLeast(1)
            val scaled = Bitmap.createScaledBitmap(bmp, w, h, true)
            ensureSpace(h + SECTION_GAP)
            canvas.drawBitmap(scaled, null, Rect(MARGIN, currentY, MARGIN + w, currentY + h), null)
            currentY += h + SECTION_GAP
            if (scaled != bmp) bmp.recycle()
            scaled.recycle()
        }

        // Parameters
        canvas.drawText("Parameter", MARGIN.toFloat(), (currentY - h2Paint.fontMetrics.top), h2Paint)
        currentY += (h2Paint.fontMetrics.bottom - h2Paint.fontMetrics.top).toInt() + 8
        drawDivider(currentY); currentY += 10
        fun drawParam(label: String, value: String?) {
            if (value.isNullOrBlank()) return
            ensureSpace(28)
            canvas.drawText(label, MARGIN.toFloat(), (currentY - labelPaint.fontMetrics.top), labelPaint)
            currentY = drawTextWrapped(value, MARGIN + 120, currentY + 2, CONTENT_RIGHT - (MARGIN + 120), textPaint)
        }
        drawParam("Maschenprobe:", params.gauge)
        drawParam("Nadeln:", params.needles)
        drawParam("Größe:", params.size)
        drawParam("Gewichtsklasse:", params.yarnWeight)
        params.notes?.let { n -> ensureSpace(28); canvas.drawText("Notizen:", MARGIN.toFloat(), (currentY - labelPaint.fontMetrics.top), labelPaint); currentY = drawTextWrapped(n, MARGIN + 120, currentY + 2, CONTENT_RIGHT - (MARGIN + 120), textPaint) }
        currentY += SECTION_GAP

        // Yarns
        canvas.drawText("Verwendete Wolle", MARGIN.toFloat(), (currentY - h2Paint.fontMetrics.top), h2Paint)
        currentY += (h2Paint.fontMetrics.bottom - h2Paint.fontMetrics.top).toInt() + 8
        drawDivider(currentY); currentY += 10

        yarns.forEach { usage ->
            ensureSpace(ROW_IMAGE_SIZE + 24)
            val yBmp = decodeBitmapOrNull(chooseImageBytes(usage.yarn.imageBytes))
            if (yBmp != null) {
                val rect = Rect(MARGIN, currentY, MARGIN + ROW_IMAGE_SIZE, currentY + ROW_IMAGE_SIZE)
                val scaled = Bitmap.createScaledBitmap(yBmp, ROW_IMAGE_SIZE, ROW_IMAGE_SIZE, true)
                canvas.drawBitmap(scaled, null, rect, null)
                if (scaled != yBmp) yBmp.recycle()
                scaled.recycle()
            } else {
                val r = Rect(MARGIN, currentY, MARGIN + ROW_IMAGE_SIZE, currentY + ROW_IMAGE_SIZE)
                canvas.drawRect(r, linePaint) // Placeholder-Rahmen
            }

            val textX = MARGIN + ROW_IMAGE_SIZE + 12
            var y = currentY
            val fm = textPaint.fontMetrics; val lh = (fm.bottom - fm.top + fm.leading).toInt()
            listOfNotNull(usage.yarn.brand, usage.yarn.name).joinToString(" · ").takeIf { it.isNotBlank() }?.let { canvas.drawText(it, textX.toFloat(), (y - textPaint.fontMetrics.top), textPaint); y += lh }
            listOfNotNull(usage.yarn.colorway, usage.yarn.lot).joinToString(" · ").takeIf { it.isNotBlank() }?.let { canvas.drawText(it, textX.toFloat(), (y - smallPaint.fontMetrics.top), smallPaint); y += (smallPaint.fontMetrics.bottom - smallPaint.fontMetrics.top + smallPaint.fontMetrics.leading).toInt() }
            listOfNotNull(usage.yarn.material, usage.yarn.weightClass).joinToString(" · ").takeIf { it.isNotBlank() }?.let { canvas.drawText(it, textX.toFloat(), (y - smallPaint.fontMetrics.top), smallPaint); y += (smallPaint.fontMetrics.bottom - smallPaint.fontMetrics.top + smallPaint.fontMetrics.leading).toInt() }
            buildString { usage.gramsUsed?.let { append("${it.toInt()} g  ") }; usage.metersUsed?.let { append("(${it.toInt()} m)") } }.takeIf { it.isNotBlank() }?.let { canvas.drawText(it, textX.toFloat(), (y - smallPaint.fontMetrics.top), smallPaint) }

            currentY = max(currentY + ROW_IMAGE_SIZE, y) + ROW_GAP
            drawDivider(currentY); currentY += 8
        }

        // Footer
        fun drawFooter() { val footer = "Seite $pageNumber"; canvas.drawText(footer, (CONTENT_RIGHT - textPaint.measureText(footer)), (PAGE_HEIGHT - MARGIN / 2f), smallPaint) }
        drawFooter()

        pdf.finishPage(page)
        val baos = ByteArrayOutputStream(); pdf.writeTo(baos); pdf.close(); baos.toByteArray()
    }
}