package org.example.project

import kotlin.math.abs
import kotlin.math.roundToInt

internal fun formatDecimal(value: Double): String {
    if (value.isNaN() || value.isInfinite()) {
        return value.toString()
    }
    val rounded = (value * 100).roundToInt()
    val integerPart = rounded / 100
    val fractionalPart = abs(rounded % 100)
    return "$integerPart.${fractionalPart.toString().padStart(2, '0')}"
}

internal fun commonFormatSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${formatDecimal(size / 1024.0)} KB"
        else -> "${formatDecimal(size / (1024.0 * 1024.0))} MB"
    }
}
