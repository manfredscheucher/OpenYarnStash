package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class Yarn(
    val id: Int,
    val name: String,
    val color: String?,
    val amount: Int,
    val url: String? = null,     // optional
    val date: String? = null     // optional, z.B. "2025-09-26"
)

@Serializable
data class Project(
    val id: Int,
    val name: String,
    val url: String? = null,
    val date: String? = null
)

@Serializable
data class Usage( // für (3) später
    val projectId: Int,
    val yarnId: Int,
    val amount: Int
)

@Serializable
data class AppData(
    val yarns: List<Yarn> = emptyList(),
    val projects: List<Project> = emptyList(),
    val usages: List<Usage> = emptyList()
)
