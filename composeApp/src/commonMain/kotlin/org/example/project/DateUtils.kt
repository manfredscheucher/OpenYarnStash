package org.example.project

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun nowInstant(): Instant = Clock.System.now()

fun nowLocalDateTime(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun nowEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()

fun getCurrentTimestamp(): String = nowInstant().toString()