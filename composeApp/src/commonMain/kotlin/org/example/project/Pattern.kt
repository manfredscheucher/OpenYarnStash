package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class Pattern(
    val id: UInt,
    val name: String,
    val creator: String? = null,
    val category: String? = null,
    val gauge: String? = null,
    val pdfId: UInt? = null,
    val modified: String? = null,
    val deleted: Boolean? = null
)
