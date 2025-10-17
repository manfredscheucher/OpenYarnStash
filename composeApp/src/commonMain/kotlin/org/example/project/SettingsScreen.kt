package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onBack: () -> Unit, onExport: () -> Unit, onImport: (String) -> Unit) {
    var showFilePicker by remember { mutableStateOf(false) }

    Column {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Button(onClick = onExport) {
            Text("Export")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showFilePicker = true }) {
            Text("Import")
        }

        if (showFilePicker) {
            FilePicker(show = true) { fileContent ->
                showFilePicker = false
                if (fileContent != null) {
                    onImport(fileContent)
                }
            }
        }
    }
}

@Composable
expect fun FilePicker(show: Boolean, onFileSelected: (String?) -> Unit)
