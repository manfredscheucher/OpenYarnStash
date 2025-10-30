package org.example.project

import kotlin.math.round

const val METERS_TO_YARDS_FACTOR = 1.09361

fun convertLength(meters: Int, toUnit: LengthUnit): Double {
    return if (toUnit == LengthUnit.YARD) {
        meters * METERS_TO_YARDS_FACTOR
    } else {
        meters.toDouble()
    }
}

fun metersFrom(value: Double, fromUnit: LengthUnit): Int {
    return if (fromUnit == LengthUnit.YARD) {
        round(value / METERS_TO_YARDS_FACTOR).toInt()
    } else {
        round(value).toInt()
    }
}
