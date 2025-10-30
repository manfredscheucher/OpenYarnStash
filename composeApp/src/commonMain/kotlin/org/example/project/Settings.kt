package org.example.project

import kotlinx.serialization.Serializable

enum class LengthUnit {
    METER, YARD
}

@Serializable
data class Settings(
    val language: String = "en",
    val projectToggles: Map<String, Boolean> = emptyMap(),
    val hideUsedYarns: Boolean = false,
    val statisticTimespan: String = "year",
    val lengthUnit: LengthUnit = LengthUnit.METER
)
