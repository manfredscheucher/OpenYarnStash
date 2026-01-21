package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class Assignment(
    val id: Int,
    val yarnId: Int,
    val projectId: Int,
    val amount: Int,
    val lastModified: String? = null
)
