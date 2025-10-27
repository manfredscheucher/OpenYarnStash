package org.example.project

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    val gauge: Int? = null,
    val madeFor: String? = null,
    val modified: String? = null,
    val rowCount: Int = 0
) {
    @Transient
    val status: ProjectStatus = when {
        endDate != null && endDate.isNotBlank() -> ProjectStatus.FINISHED
        startDate != null && startDate.isNotBlank() -> ProjectStatus.IN_PROGRESS
        else -> ProjectStatus.PLANNING
    }
}
