package org.example.project

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    val storagePlace: String? = null,
    val imageIds: List<Int> = emptyList(),
    @Transient val usedAmount: Int = 0
) {
    @Transient
    val availableAmount: Int
        get() = (amount - usedAmount).coerceAtLeast(0)

    @Transient
    val usedMeterage: Int?
        get() {
            return if (meteragePerSkein != null && weightPerSkein != null && weightPerSkein > 0) {
                (usedAmount.toLong() * meteragePerSkein) / weightPerSkein
            } else {
                null
            }?.toInt()
        }

    @Transient
    val availableMeterage: Int?
        get() {
            return if (meteragePerSkein != null && weightPerSkein != null && weightPerSkein > 0) {
                (availableAmount.toLong() * meteragePerSkein) / weightPerSkein
            } else {
                null
            }?.toInt()
        }

    fun copyForColor(): Yarn {
        return Yarn(
            id = -1,
            name = this.name,
            brand = this.brand,
            blend = this.blend,
            meteragePerSkein = this.meteragePerSkein,
            weightPerSkein = this.weightPerSkein,
            modified = getCurrentTimestamp()
        )
    }
}
