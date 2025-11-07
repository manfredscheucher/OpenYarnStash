
package org.example.project

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
fun nowInstant(): Instant = kotlin.time.Clock.System.now()

@OptIn(ExperimentalTime::class)
fun getCurrentTimestamp(): String = nowInstant().toString()

fun normalizeDateString(input: String): String? {
    val trimmed = input.trim()
    if (trimmed.isBlank()) return null
    val yyyyRegex = "^\\d{4}$".toRegex()
    val yyyyMmRegex = "^\\d{4}-(0[1-9]|1[0-2])$".toRegex()
    val yyyyMmDdRegex = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$".toRegex()
    return when {
        yyyyRegex.matches(trimmed) -> "$trimmed-01-01"
        yyyyMmRegex.matches(trimmed) -> "$trimmed-01"
        yyyyMmDdRegex.matches(trimmed) -> trimmed
        else -> null
    }
}