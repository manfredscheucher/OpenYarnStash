package org.example.project

import androidx.compose.ui.graphics.Color

// Palette generated using https://medialab.github.io/iwanthue/
object ColorPalette {
    val colors = listOf(
        "#cdf79e",
        "#edddf6",
        "#e1f433",
        "#cee6f5",
        "#9ffa5c",
        "#fadcd9",
        "#e7f158",
        "#5df9ee",
        "#e4f170",
        "#9bf1ee",
        "#d6f485",
        "#c9f2e9",
        "#7ffba7",
        "#fde096",
        "#62fbd2",
        "#f2ed9c",
        "#deefd6",
        "#e0eead",
        "#b9f3c2",
        "#eaeac1"
    ).map { Color(it.removePrefix("#").toLong(16) or 0x00000000FF000000) }

    fun idToColor(id: Int): Color {
        if (colors.isEmpty()) {
            return Color.Transparent
        }
        val index = id % colors.size
        return colors[index]
    }
}
