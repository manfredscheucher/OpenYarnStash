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
        val commitText = GeneratedVersionInfo.COMMIT_SHA.let { sha -> "${sha.take(7)} (${GeneratedVersionInfo.IS_DIRTY})" }
        Text("Commit: $commitText")
        Text("Commit Date: ${GeneratedVersionInfo.COMMIT_DATE}")
        Text("Compiled: ${GeneratedVersionInfo.BUILD_DATE}")
    }
}
