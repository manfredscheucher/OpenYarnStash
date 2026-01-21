package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class AppData(
    val yarns: MutableList<Yarn> = mutableListOf(),
    val projects: MutableList<Project> = mutableListOf(),
    val assignments: MutableList<Assignment> = mutableListOf(),
    val patterns: MutableList<Pattern> = mutableListOf()
)
