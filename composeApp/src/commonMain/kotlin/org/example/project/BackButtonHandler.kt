package org.example.project

import androidx.compose.runtime.Composable

@Composable
expect fun BackButtonHandler(enabled: Boolean = true, onBack: () -> Unit)
