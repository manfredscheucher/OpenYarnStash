package org.example.project

actual fun createPdfManager(fileHandler: FileHandler): PdfManager {
    // Use the base PdfManager implementation - it works with localStorage via FileHandler
    return PdfManager(fileHandler)
}
