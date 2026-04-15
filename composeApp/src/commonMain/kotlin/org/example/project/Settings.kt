package org.example.project

import kotlinx.serialization.Serializable

enum class LengthUnit {
    METER, YARD
}

enum class LogLevel {
    OFF,
    FATAL,
    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE
}

@Serializable
data class VersionInfo(
    val appVersion: String = "",
    val commitHash: String = "",
    val commitDate: String = "",
    val lastUsedDate: String = ""
)

@Serializable
data class Settings(
    val language: String = "en",
    val projectToggles: Map<String, Boolean> = emptyMap(),
    val hideUsedYarns: Boolean = false,
    val statisticTimespan: String = "year",
    val lengthUnit: LengthUnit = LengthUnit.METER,
    val logLevel: LogLevel = LogLevel.ERROR,
    val versionInfo: VersionInfo = VersionInfo()
)
