package org.example.project

import androidx.compose.ui.graphics.Color

// Palette generated using https://medialab.github.io/iwanthue/
object ColorPalette {
    val colors = listOf(
        "#9deee5",
        "#ecaa9a",
        "#53c6ef",
        "#e8e7b0",
        "#74aff3",
        "#a1bb7a",
        "#c4b7ea",
        "#8ac793",
        "#eaaecf",
        "#80d4ba",
        "#8dc8f9",
        "#b9d79e",
        "#a1bde6",
        "#bbbf8b",
        "#55cdd8",
        "#dcbe96",
        "#8cd1e5",
        "#bdefc5",
        "#8dc3b8",
        "#98c3a6"
    ).map { Color((it.removePrefix("#").toLong(16) or 0xE6000000L).toInt()) }

    fun idToColor(id: Int): Color {
        if (colors.isEmpty()) {
            return Color.Transparent
        }
        val index = id % colors.size
        return colors[index]
    }
}
