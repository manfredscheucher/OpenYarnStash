package org.example.project

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class Yarn(
    val id: Int,
    val name: String,
    val color: String? = null,
    val colorCode: String? = null,
    val brand: String? = null,
    val amount: Int = 0,
    val blend: String? = null,
    val dyeLot: String? = null,
    val notes: String? = null,
    val weightPerSkein: Int? = null,
    val meteragePerSkein: Int? = null,
    val added: String? = null,
    val modified: String? = null,
    val storagePlace: String? = null
)
