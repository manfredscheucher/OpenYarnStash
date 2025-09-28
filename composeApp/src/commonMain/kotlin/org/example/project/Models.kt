package org.example.project

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames // Import für JsonNames

@Serializable
data class Yarn(
    val id: Int,
    val name: String,
    val color: String? = null,
    val brand: String? = null,
    val amount: Int = 0,
    val url: String? = null,
    @JsonNames("date") // Für Abwärtskompatibilität mit alten Speicherdaten
    val dateAdded: String? = null, // Umbenannt von 'date'
    val notes: String? = null // Added notes for Yarn
)

@Serializable
data class Project(
    val id: Int,
    val name: String,
    val url: String? = null,
    val date: String? = null, // Bleibt unverändert
    val notes: String? = null // Added notes for Project
)

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
