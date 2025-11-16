package org.example.project

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.project.pdf.PdfThumbnailGeneratorFactory

class PdfManager(private val fileHandler: FileHandler) {

    private val pdfsDir = "pdf/pattern"
    private val pdfThumbnailsDir = "pdf/pattern/thumbnails"

    private val thumbnailGenerator by lazy { PdfThumbnailGeneratorFactory.create() }

    companion object {
        private const val THUMBNAIL_WIDTH = 256
        private const val THUMBNAIL_HEIGHT = 256
    }

    private fun getPatternPdfPath(patternId: Int): String {
        return     "$pdfsDir/$patternId.pdf"
    }
    private fun getPatternPdfThumbnailPath(patternId: Int): String {
        return "$pdfThumbnailsDir/${patternId}_${THUMBNAIL_WIDTH}x${THUMBNAIL_HEIGHT}.png"
    }

    suspend fun savePatternPdf(patternId: Int, pdfBytes: ByteArray): Int {
        val pdfId = 1 // Since we only have one pdf per pattern, the id is always 1
        withContext(Dispatchers.Default) {
            fileHandler.writeBytes("$pdfsDir/$patternId.pdf", pdfBytes)
        }
        // Generate and save a thumbnail right away
        generateAndSaveThumbnail(patternId, pdfBytes)
        return pdfId
    }

    suspend fun getPatternPdf(patternId: Int): ByteArray? {
        return withContext(Dispatchers.Default) {
            fileHandler.readBytes("$pdfsDir/$patternId.pdf")
        }
    }

    suspend fun getPatternPdfThumbnail(patternId: Int, maxWidth: Int = THUMBNAIL_WIDTH, maxHeight: Int = THUMBNAIL_HEIGHT): ByteArray? {
        val thumbnailPath = getPatternPdfThumbnailPath(patternId)
        var thumbnailBytes = withContext(Dispatchers.Default) {
            fileHandler.readBytes(thumbnailPath)
        }

        if (thumbnailBytes == null) {
            val pdfBytes = getPatternPdf(patternId)
            if (pdfBytes != null) {
                thumbnailBytes = thumbnailGenerator.generateThumbnail(pdfBytes, maxWidth, maxHeight)
                if (thumbnailBytes != null) {
                    withContext(Dispatchers.Default) {
                        fileHandler.writeBytes(thumbnailPath, thumbnailBytes)
                    }
                }
            }
        }
        return thumbnailBytes
    }


    private suspend fun generateAndSaveThumbnail(patternId: Int, pdfBytes: ByteArray, width: Int = THUMBNAIL_WIDTH, height: Int = THUMBNAIL_HEIGHT) {
        val thumbnailBytes = thumbnailGenerator.generateThumbnail(pdfBytes, width, height)
        if (thumbnailBytes != null) {
            val thumbnailPath = getPatternPdfThumbnailPath(patternId)
            withContext(Dispatchers.Default) {
                fileHandler.writeBytes(thumbnailPath, thumbnailBytes)
            }
        }
    }

    suspend fun deletePatternPdf(patternId: Int) {
        withContext(Dispatchers.Default) {
            fileHandler.deleteFile(getPatternPdfPath(patternId))
            // Also delete the thumbnail
            fileHandler.deleteFile(getPatternPdfThumbnailPath(patternId))
        }
    }
}
