package org.example.project

sealed interface Screen {
    data object Home : Screen
    data object YarnList : Screen
    data class YarnForm(val yarnId: Int?) : Screen
}
