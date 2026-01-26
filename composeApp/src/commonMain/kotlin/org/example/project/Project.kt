package org.example.project

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

enum class ProjectStatus {
    PLANNING,
    IN_PROGRESS,
    FINISHED
}

@Serializable
data class RowCounter(
    val name: String,
    val value: Int
)

@Serializable
data class Project(
    val id: UInt,
    val name: String,
    val startDate: String? = null,
    val endDate: String? = null,
    val notes: String? = null,
    val needleSize: String? = null,
    val size: String? = null,
    val gauge: String? = null,
    val madeFor: String? = null,
    val modified: String? = null,
    val deleted: Boolean? = null,
    val rowCounters: List<RowCounter> = emptyList(),
    val patternId: UInt? = null,
    val imageIds: List<UInt> = emptyList(),
    @Transient val imagesChanged: Boolean = false
) {
    @Transient
    val status: ProjectStatus = when {
        endDate != null && endDate.isNotBlank() -> ProjectStatus.FINISHED
        startDate != null && startDate.isNotBlank() -> ProjectStatus.IN_PROGRESS
        else -> ProjectStatus.PLANNING
    }
}
