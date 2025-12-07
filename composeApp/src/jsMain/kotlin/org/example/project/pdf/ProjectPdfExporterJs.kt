package org.example.project.pdf

import org.example.project.ImageManager

class ProjectPdfExporterJs : ProjectPdfExporter {
    override suspend fun exportToPdf(project: Project, params: Params, yarns: List<YarnUsage>, imageManager: ImageManager): ByteArray {
        throw NotImplementedError("PDF export is not yet implemented for JS")
    }
}
