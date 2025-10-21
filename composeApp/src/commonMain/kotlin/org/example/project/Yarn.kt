package org.example.project

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class Yarn(
    @JsonNames("lastModified")
    val modified: String,
    val id: Int,
    val name: String,
    val color: String? = null,
    val brand: String? = null,
    val amount: Int = 0, // Gesamte Menge in Gramm
    val blend: String? = null,
    @JsonNames("colorLot")
    val dyeLot: String? = null, // Farbpartie
    val notes: String? = null, // Added notes for Yarn
    @JsonNames("gramsPerBall")
    val weightPerSkein: Int? = null, // Gramm pro Knäuel
    @JsonNames("metersPerBall")
    val meteragePerSkein: Int? = null, // Meter pro Knäuel
    val dateAdded: String? = null
)
