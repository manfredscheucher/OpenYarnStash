package org.example.project

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.project.pdf.PdfThumbnailGeneratorFactory

open class PdfManager(private val fileHandler: FileHandler) {

    private val pdfsDir = "pdf/pattern"
    private val pdfThumbnailsDir = "pdf/pattern/thumbnails"

    private val thumbnailGenerator by lazy { PdfThumbnailGeneratorFactory.create() }

    companion object {
        const val THUMBNAIL_WIDTH = 256
        const val THUMBNAIL_HEIGHT = 256
        const val LARGE_THUMBNAIL_WIDTH = 2048
        const val LARGE_THUMBNAIL_HEIGHT = 2048
    }

    private fun getPatternPdfPath(patternId: Int): String {
        return "$pdfsDir/$patternId.pdf"
    }

    private fun getPatternPdfThumbnailPath(patternId: Int, width: Int, height: Int): String {
        return "$pdfThumbnailsDir/${patternId}_${width}x${height}.png"
    }

    open suspend fun savePatternPdf(patternId: Int, pdfBytes: ByteArray): Int {
        val pdfId = 1 // Since we only have one pdf per pattern, the id is always 1
        withContext(Dispatchers.Default) {
            fileHandler.writeBytes(getPatternPdfPath(patternId), pdfBytes)
        }
        // Generate and save thumbnails right away
        generateAndSaveThumbnail(patternId, pdfBytes, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
        generateAndSaveThumbnail(patternId, pdfBytes, LARGE_THUMBNAIL_WIDTH, LARGE_THUMBNAIL_HEIGHT)
        return pdfId
    }

    open suspend fun getPatternPdf(patternId: Int): ByteArray? {
        return withContext(Dispatchers.Default) {
            fileHandler.readBytes(getPatternPdfPath(patternId))
        }
    }

    open fun openPatternPdfExternally(patternId: Int) {
        val path = getPatternPdfPath(patternId)
        fileHandler.openFileExternally(path)
    }

    open suspend fun getPatternPdfThumbnail(patternId: Int, width: Int = THUMBNAIL_WIDTH, height: Int = THUMBNAIL_HEIGHT): ByteArray? {
        val thumbnailPath = getPatternPdfThumbnailPath(patternId, width, height)
        var thumbnailBytes = withContext(Dispatchers.Default) {
            fileHandler.readBytes(thumbnailPath)
        }

        if (thumbnailBytes == null) {
            val pdfBytes = getPatternPdf(patternId)
            if (pdfBytes != null) {
                thumbnailBytes = thumbnailGenerator.generateThumbnail(pdfBytes, width, height)
                if (thumbnailBytes != null) {
                    withContext(Dispatchers.Default) {
                        fileHandler.writeBytes(thumbnailPath, thumbnailBytes)
                    }
                }
            }
        }
        return thumbnailBytes
    }

    private suspend fun generateAndSaveThumbnail(patternId: Int, pdfBytes: ByteArray, width: Int, height: Int) {
        val thumbnailBytes = thumbnailGenerator.generateThumbnail(pdfBytes, width, height)
        if (thumbnailBytes != null) {
            val thumbnailPath = getPatternPdfThumbnailPath(patternId, width, height)
            withContext(Dispatchers.Default) {
                fileHandler.writeBytes(thumbnailPath, thumbnailBytes)
            }
        }
    }

    open suspend fun deletePatternPdf(patternId: Int) {
        withContext(Dispatchers.Default) {
            fileHandler.deleteFile(getPatternPdfPath(patternId))
            // Also delete the thumbnails
            fileHandler.deleteFile(getPatternPdfThumbnailPath(patternId, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT))
            fileHandler.deleteFile(getPatternPdfThumbnailPath(patternId, LARGE_THUMBNAIL_WIDTH, LARGE_THUMBNAIL_HEIGHT))
        }
    }
}
