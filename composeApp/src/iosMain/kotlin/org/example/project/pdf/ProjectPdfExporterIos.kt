package org.example.project.pdf

import kotlinx.cinterop.*
import org.example.project.ImageManager
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.UIKit.*
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
class ProjectPdfExporterIos : ProjectPdfExporter {
    override suspend fun exportToPdf(project: Project, params: Params, yarns: List<YarnUsage>, imageManager: ImageManager): ByteArray = memScoped {
        val data = NSMutableData()
        val pageRect = CGRectMake(0.0, 0.0, 595.0, 842.0)
        UIGraphicsBeginPDFContextToData(data, pageRect, null)
        UIGraphicsBeginPDFPage()
        val ctx = UIGraphicsGetCurrentContext()!!

        fun drawText(text: String, x: Double, y: Double, font: UIFont, color: UIColor = UIColor.blackColor) : Double {
            val attrs = mapOf<Any?, Any?>(NSFontAttributeName to font, NSForegroundColorAttributeName to color)
            val ns = text as NSString
            ns.drawAtPoint(CGPointMake(x, y), attrs)
            return y + font.lineHeight
        }
        fun drawWrapped(text: String, x: Double, yStart: Double, maxWidth: Double, font: UIFont): Double {
            val paragraph = NSMutableParagraphStyle().apply { setLineBreakMode(NSLineBreakByWordWrapping); setAlignment(NSTextAlignmentLeft) }
            val attrs = mutableMapOf<Any?, Any?>(
                NSFontAttributeName to font,
                NSForegroundColorAttributeName to UIColor.blackColor,
                NSParagraphStyleAttributeName to paragraph
            )
            val ns = text as NSString
            val bounding = ns.boundingRectWithSize(CGSizeMake(maxWidth, Double.MAX_VALUE), options = NSStringDrawingUsesLineFragmentOrigin, attributes = attrs, context = null)
            val height = bounding.useContents { size.height }
            ns.drawWithRect(CGRectMake(x, yStart, maxWidth, height), options = NSStringDrawingUsesLineFragmentOrigin, attributes = attrs, context = null)
            return yStart + height + 4.0
        }
        fun drawImage(bytes: ByteArray, rect: CValue<CGRect>) { UIImage(data = bytes.toNSData())?.drawInRect(rect) }
        fun drawDivider(y: Double) { CGContextSetStrokeColorWithColor(ctx, UIColor.lightGrayColor.CGColor); CGContextSetLineWidth(ctx, 1.0); CGContextMoveToPoint(ctx, 36.0, y); CGContextAddLineToPoint(ctx, 559.0, y); CGContextStrokePath(ctx) }

        var y = 36.0
        y = drawText(project.title, 36.0, y, UIFont.boldSystemFontOfSize(20.0)) + 12.0

        project.imageIds.firstOrNull()?.let { imageId ->
            imageManager.getProjectImage(project.id, imageId)?.let { bytes ->
                val img = UIImage(data = bytes.toNSData())
                img?.let { im ->
                    im.size.useContents {
                        val ratio = kotlin.math.min(240.0 / width, 180.0 / height)
                        val w = width * ratio
                        val h = height * ratio
                        im.drawInRect(CGRectMake(36.0, y, w, h))
                        y += h + 12.0
                    }
                }
            }
        }

        y = drawText("Parameter", 36.0, y, UIFont.boldSystemFontOfSize(14.0))
        drawDivider(y); y += 10.0
        fun param(label: String, value: String?) { if (!value.isNullOrBlank()) { y = drawText(label, 36.0, y, UIFont.systemFontOfSize(10.0)); y = drawWrapped(value!!, 36.0 + 120.0, y - 12.0, 559.0 - (36.0 + 120.0), UIFont.systemFontOfSize(10.0)) } }
        param("Maschenprobe:", params.gauge)
        param("Nadeln:", params.needles)
        param("Größe:", params.size)
        param("Gewichtsklasse:", params.yarnWeight)
        params.notes?.let { n -> y = drawText("Notizen:", 36.0, y, UIFont.systemFontOfSize(10.0)); y = drawWrapped(n, 36.0 + 120.0, y - 12.0, 559.0 - (36.0 + 120.0), UIFont.systemFontOfSize(10.0)) }
        y += 12.0
        y = drawText("Verwendete Wolle", 36.0, y, UIFont.boldSystemFontOfSize(14.0))
        drawDivider(y); y += 10.0

        yarns.forEach { usage ->
            usage.yarn.imageIds.firstOrNull()?.let { imageId ->
                imageManager.getYarnImage(usage.yarn.id, imageId)?.let { ib -> drawImage(ib, CGRectMake(36.0, y, 72.0, 72.0)) }
            }
            val startX = 36.0 + 72.0 + 12.0
            var localY = y
            listOfNotNull(usage.yarn.brand, usage.yarn.name).joinToString(" · ").takeIf { it.isNotBlank() }?.let { localY = drawText(it, startX, localY, UIFont.systemFontOfSize(10.0)) }
            listOfNotNull(usage.yarn.colorway, usage.yarn.lot).joinToString(" · ").takeIf { it.isNotBlank() }?.let { localY = drawText(it, startX, localY, UIFont.systemFontOfSize(9.0)) }
            listOfNotNull(usage.yarn.material, usage.yarn.weightClass).joinToString(" · ").takeIf { it.isNotBlank() }?.let { localY = drawText(it, startX, localY, UIFont.systemFontOfSize(9.0)) }
            buildString { usage.gramsUsed?.let { append("${it.toInt()} g  ") }; usage.metersUsed?.let { append("(${it.toInt()} m)") } }.takeIf { it.isNotBlank() }?.let { localY = drawText(it, startX, localY, UIFont.systemFontOfSize(9.0)) }
            y = kotlin.math.max(y + 72.0, localY) + 8.0
            drawDivider(y); y += 8.0
            if (y > 842.0 - 72.0) { UIGraphicsBeginPDFPage(); y = 36.0 }
        }

        UIGraphicsEndPDFContext()
        data.toByteArray()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { NSData.create(bytes = it.addressOf(0), length = size.toULong()) }

@OptIn(ExperimentalForeignApi::class)
private fun NSMutableData.toByteArray(): ByteArray = ByteArray(length.toInt()).also { dest ->
    dest.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, length)
    }
}
