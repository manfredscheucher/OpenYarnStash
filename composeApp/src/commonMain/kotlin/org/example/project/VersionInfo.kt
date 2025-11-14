package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VersionInfoView() {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("Version: ${GeneratedVersionInfo.VERSION}")
        val commitText = GeneratedVersionInfo.GIT_SHA?.let { sha ->
            if (GeneratedVersionInfo.IS_DIRTY) "$sha (dirty)" else sha
        } ?: "(unknown)"
        Text("Commit: $commitText")
    }
}
