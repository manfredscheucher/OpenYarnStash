package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class Yarn(
    val id: Int,
    val name: String,
    val color: String? = null,
    val amount: Int = 0,
    val url: String? = null,
    val date: String? = null
)

@Serializable
data class Project(
    val id: Int,
    val name: String,
    val url: String? = null,
    val date: String? = null
)

@Serializable
data class Usage(
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
