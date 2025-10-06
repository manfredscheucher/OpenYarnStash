package org.example.project

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonNames

@Serializable
data class Yarn(
    val id: Int,
    val name: String,
    val color: String? = null,
    val brand: String? = null,
    val amount: Int = 0, // Gesamte Menge in Gramm
    val colorLot: String? = null, // Farbcharge
    val url: String? = null,
    val dateAdded: String? = null,
    val notes: String? = null, // Added notes for Yarn
    val gramsPerBall: Int? = null, // Gramm pro Knäuel
    val metersPerBall: Int? = null // Meter pro Knäuel
)

enum class ProjectStatus {
    PLANNING,
    IN_PROGRESS,
    FINISHED
}

@Serializable
data class Project(
    val id: Int,
    val name: String,
    val url: String? = null,
    @JsonNames("date") // Maps old "date" field to "startDate" for backward compatibility
    val startDate: String? = null,
    val endDate: String? = null,
    val notes: String? = null // Added notes for Project
) {
    @Transient
    val status: ProjectStatus = when {
        endDate != null && endDate.isNotBlank() -> ProjectStatus.FINISHED
        startDate != null && startDate.isNotBlank() -> ProjectStatus.IN_PROGRESS
        else -> ProjectStatus.PLANNING
    }
}

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
