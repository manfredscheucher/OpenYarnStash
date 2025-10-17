package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class Yarn(
    val lastModified: String,
    val id: Int,
    val name: String,
    val color: String? = null,
    val brand: String? = null,
    val amount: Int = 0, // Gesamte Menge in Gramm
    val colorLot: String? = null, // Farbpartie
    val notes: String? = null, // Added notes for Yarn
    val gramsPerBall: Int? = null, // Gramm pro Knäuel
    val metersPerBall: Int? = null, // Meter pro Knäuel
    val dateAdded: String? = null
)
