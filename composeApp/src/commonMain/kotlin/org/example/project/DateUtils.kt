
package org.example.project

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
fun nowInstant(): Instant = kotlin.time.Clock.System.now()

@OptIn(ExperimentalTime::class)
fun getCurrentTimestamp(): String = nowInstant().toString()