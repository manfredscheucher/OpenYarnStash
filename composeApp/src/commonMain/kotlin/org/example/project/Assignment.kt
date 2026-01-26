package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class Assignment(
    val id: UInt,
    val yarnId: UInt,
    val projectId: UInt,
    val amount: Int,
    val lastModified: String? = null,
    val deleted: Boolean? = null
)
