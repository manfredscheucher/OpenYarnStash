package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import openyarnstash.composeapp.generated.resources.Res
import openyarnstash.composeapp.generated.resources.export_json
import openyarnstash.composeapp.generated.resources.import_json
import openyarnstash.composeapp.generated.resources.settings_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, onExport: () -> Unit, onImport: (String) -> Unit) {
    var showFilePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            Button(onClick = onExport, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.export_json))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { showFilePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.import_json))
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
}

@Composable
expect fun FilePicker(show: Boolean, onFileSelected: (String?) -> Unit)
