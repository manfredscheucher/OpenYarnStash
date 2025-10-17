package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class AppData(
    val yarns: List<Yarn> = emptyList(),
    val projects: List<Project> = emptyList(),
    val usages: List<Usage> = emptyList()
)
