package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class Settings(val language: String = "en")
