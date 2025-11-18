
package org.example.project

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
fun nowInstant(): Instant = kotlin.time.Clock.System.now()

@OptIn(ExperimentalTime::class)
fun getCurrentTimestamp(): String = nowInstant().toString()

@OptIn(ExperimentalTime::class)
fun formatTimestamp(timestamp: String): String {
    val instant = Instant.parse(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val date = localDateTime.date
    val month = localDateTime.monthNumber.toString().padStart(2, '0')
    val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')
    val second = localDateTime.second.toString().padStart(2, '0')
    val local = "${date.year}-${month}-${day} ${hour}:${minute}:${second}"
    val utc = timestamp.substring(0, 16).replace("T", " ")
    return "$local ($utc UTC)"
}

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