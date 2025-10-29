package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val language: String = "en",
    val projectToggles: Map<String, Boolean> = emptyMap(),
    val hideUsedYarns: Boolean = false,
    val statisticTimespan: String = "year"
)
