package org.example.project.pdf

// PDF-Exporter-specific data models
// These are tailored for the PDF generation logic.
// We will map the application's domain models to these before exporting.

data class Project(val id: UInt, val title: String, val imageIds: List<UInt>)
data class Params(val gauge: String?, val needles: String?, val size: String?, val yarnWeight: String?, val notes: String?)
data class Yarn(val id: UInt, val brand: String?, val name: String?, val colorway: String?, val lot: String?, val material: String?, val weightClass: String?, val imageIds: List<UInt>)
data class YarnUsage(val yarn: Yarn, val metersUsed: Double?, val gramsUsed: Double?)
