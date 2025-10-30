package org.example.project

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfManager(private val fileHandler: FileHandler) {

    private val pdfsDir = "pdf/pattern"

    suspend fun savePdf(patternId: Int, pdfBytes: ByteArray) {
        withContext(Dispatchers.Default) {
            fileHandler.writeBytes("$pdfsDir/$patternId.pdf", pdfBytes)
        }
    }

    suspend fun getPdf(patternId: Int): ByteArray? {
        return withContext(Dispatchers.Default) {
            fileHandler.readBytes("$pdfsDir/$patternId.pdf")
        }
    }

    suspend fun deletePdf(patternId: Int) {
        withContext(Dispatchers.Default) {
            fileHandler.deleteFile("$pdfsDir/$patternId.pdf")
        }
    }
}
