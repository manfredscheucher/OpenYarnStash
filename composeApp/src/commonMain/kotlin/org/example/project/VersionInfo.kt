package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// These values are now provided by the generated GeneratedVersionInfo object.
// The constants here will reference the generated values.
const val VERSION_NAME = GeneratedVersionInfo.VERSION
const val GIT_HASH = GeneratedVersionInfo.GIT_SHA
const val IS_DIRTY = GeneratedVersionInfo.IS_DIRTY

@Composable
fun VersionInfoView() {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("Version: $VERSION_NAME")
        val commitText = if (IS_DIRTY) "$GIT_HASH (dirty)" else GIT_HASH
        Text("Commit: $commitText")
    }
}
