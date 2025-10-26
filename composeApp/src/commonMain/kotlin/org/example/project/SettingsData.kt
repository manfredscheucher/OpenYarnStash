package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class SettingsData(val language: String = "en")
