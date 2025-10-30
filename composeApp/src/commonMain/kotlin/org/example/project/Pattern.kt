package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class Pattern(
    val id: Int,
    val name: String,
    val creator: String? = null,
    val gauge: String? = null
)
