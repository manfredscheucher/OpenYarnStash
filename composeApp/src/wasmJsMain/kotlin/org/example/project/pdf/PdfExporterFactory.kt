package org.example.project.pdf

import org.example.project.ImageManager
import org.example.project.Project
import org.example.project.YarnUsage

actual fun getProjectPdfExporter(): ProjectPdfExporter {
    return object : ProjectPdfExporter {
        override suspend fun exportToPdf(
            project: Project,
            params: Params,
            yarns: List<YarnUsage>,
            imageManager: ImageManager
        ): ByteArray {
            return ByteArray(0)
        }
    }
}
