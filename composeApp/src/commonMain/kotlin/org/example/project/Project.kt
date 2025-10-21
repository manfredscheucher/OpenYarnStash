package org.example.project

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonNames

enum class ProjectStatus {
    PLANNING,
    IN_PROGRESS,
    FINISHED
}

@Serializable
data class Project(
    val id: Int,
    val name: String,
    val startDate: String? = null,
    val endDate: String? = null,
    val notes: String? = null,
    val needleSize: String? = null,
    val size: String? = null,
    val gauge: String? = null,
    val modified: String? = null
) {
    @Transient
    val status: ProjectStatus = when {
        endDate != null && endDate.isNotBlank() -> ProjectStatus.FINISHED
        startDate != null && startDate.isNotBlank() -> ProjectStatus.IN_PROGRESS
        else -> ProjectStatus.PLANNING
    }
}
