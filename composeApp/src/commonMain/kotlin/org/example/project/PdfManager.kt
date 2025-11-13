package org.example.project

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfManager(private val fileHandler: FileHandler) {

    private val pdfsDir = "pdf/pattern"

    suspend fun savePatternPdf(patternId: Int, pdfBytes: ByteArray): Int {
        val pdfId = 1 // Since we only have one pdf per pattern, the id is always 1
        withContext(Dispatchers.Default) {
            fileHandler.writeBytes("$pdfsDir/$patternId.pdf", pdfBytes)
        }
        return pdfId
    }

    suspend fun getPatternPdf(patternId: Int): ByteArray? {
        return withContext(Dispatchers.Default) {
            fileHandler.readBytes("$pdfsDir/$patternId.pdf")
        }
    }

    suspend fun deletePatternPdf(patternId: Int) {
        withContext(Dispatchers.Default) {
            fileHandler.deleteFile("$pdfsDir/$patternId.pdf")
        }
    }
}
