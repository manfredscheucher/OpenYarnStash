package org.example.project

actual fun createPdfManager(fileHandler: FileHandler): PdfManager {
    return object : PdfManager(fileHandler) {
        override suspend fun savePatternPdf(patternId: Int, pdfBytes: ByteArray): Int {
            throw UnsupportedOperationException("PDF export is not supported on this platform")
        }

        override suspend fun getPatternPdf(patternId: Int): ByteArray? {
            throw UnsupportedOperationException("PDF export is not supported on this platform")
        }

        override fun openPatternPdfExternally(patternId: Int) {
            throw UnsupportedOperationException("PDF export is not supported on this platform")
        }

        override suspend fun getPatternPdfThumbnail(
            patternId: Int,
            width: Int,
            height: Int
        ): ByteArray? {
            throw UnsupportedOperationException("PDF export is not supported on this platform")
        }

        override suspend fun deletePatternPdf(patternId: Int) {
            throw UnsupportedOperationException("PDF export is not supported on this platform")
        }
    }
}
