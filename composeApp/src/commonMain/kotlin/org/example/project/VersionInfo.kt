package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

const val GIT_HASH = "a5b658fba261d840f7dba6721cfe7bc6d832e059"
const val IS_DIRTY = true

@Composable
fun VersionInfoView() {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("Version: $VERSION_NAME")
        val commitText = if (IS_DIRTY) "$GIT_HASH (dirty)" else GIT_HASH
        Text("Commit: $commitText")
    }
}
