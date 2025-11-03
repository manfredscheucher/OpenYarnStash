package org.example.project

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val Bordeaux = Color(0xFF6D071A)

val DarkColorScheme = darkColorScheme(
    primary = Bordeaux,
    secondary = Color(0xFFC6C6C6),
    tertiary = Color(0xFF9E9E9E)
)

val LightColorScheme = lightColorScheme(
    primary = Bordeaux,
    secondary = Color(0xFF616161),
    tertiary = Color(0xFF9E9E9E)
)
