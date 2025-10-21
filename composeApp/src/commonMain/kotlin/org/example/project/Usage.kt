package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class Usage(
    val projectId: Int,
    val yarnId: Int,
    val amount: Int,
    val modified: String? = null
)
