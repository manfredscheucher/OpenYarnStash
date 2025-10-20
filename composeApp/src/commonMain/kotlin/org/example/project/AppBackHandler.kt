package org.example.project

import androidx.compose.runtime.Composable

@Composable
expect fun AppBackHandler(enabled: Boolean = true, onBack: () -> Unit)
