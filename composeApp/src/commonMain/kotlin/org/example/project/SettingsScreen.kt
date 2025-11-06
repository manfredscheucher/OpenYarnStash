package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import openyarnstash.composeapp.generated.resources.Res
import openyarnstash.composeapp.generated.resources.common_cancel
import openyarnstash.composeapp.generated.resources.common_yes
import openyarnstash.composeapp.generated.resources.export_json
import openyarnstash.composeapp.generated.resources.export_zip
import openyarnstash.composeapp.generated.resources.import_dialog_message
import openyarnstash.composeapp.generated.resources.import_dialog_title
import openyarnstash.composeapp.generated.resources.import_json
import openyarnstash.composeapp.generated.resources.import_zip
import openyarnstash.composeapp.generated.resources.language_label
import openyarnstash.composeapp.generated.resources.settings
import openyarnstash.composeapp.generated.resources.settings_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentLocale: String,
    currentLengthUnit: LengthUnit,
    onBack: () -> Unit,
    onExport: () -> Unit,
    onExportZip: () -> Unit,
    onImport: (String) -> Unit,
    onImportZip: (ByteArray) -> Unit,
    onLocaleChange: (String) -> Unit,
    onLengthUnitChange: (LengthUnit) -> Unit
) {
    var showJsonFilePicker by remember { mutableStateOf(false) }
    var showZipFilePicker by remember { mutableStateOf(false) }
    var showImportJsonConfirmDialog by remember { mutableStateOf(false) }
    var showImportZipConfirmDialog by remember { mutableStateOf(false) }
    var languageDropdownExpanded by remember { mutableStateOf(false) }
    var lengthUnitDropdownExpanded by remember { mutableStateOf(false) }

    AppBackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(Res.drawable.settings),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.settings_title))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                ExposedDropdownMenuBox(
                    expanded = languageDropdownExpanded,
                    onExpandedChange = { languageDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = if (currentLocale == "de") "Deutsch" else "English",
                        onValueChange = {},
                        label = { Text(stringResource(Res.string.language_label)) },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = languageDropdownExpanded,
                        onDismissRequest = { languageDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Deutsch") },
                            onClick = {
                                onLocaleChange("de")
                                languageDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("English") },
                            onClick = {
                                onLocaleChange("en")
                                languageDropdownExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = lengthUnitDropdownExpanded,
                    onExpandedChange = { lengthUnitDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = if (currentLengthUnit == LengthUnit.METER) "Meters" else "Yards", // TODO: Localize
                        onValueChange = {},
                        label = { Text("Length Unit") }, // TODO: Localize
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lengthUnitDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = lengthUnitDropdownExpanded,
                        onDismissRequest = { lengthUnitDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Meters") }, // TODO: Localize
                            onClick = {
                                onLengthUnitChange(LengthUnit.METER)
                                lengthUnitDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Yards") }, // TODO: Localize
                            onClick = {
                                onLengthUnitChange(LengthUnit.YARD)
                                lengthUnitDropdownExpanded = false
                            }
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onExport, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.export_json))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onExportZip, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.export_zip))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { showImportJsonConfirmDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.import_json))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { showImportZipConfirmDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.import_zip))
                }

                if (showImportJsonConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { showImportJsonConfirmDialog = false },
                        title = { Text(stringResource(Res.string.import_dialog_title)) },
                        text = { Text(stringResource(Res.string.import_dialog_message)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showImportJsonConfirmDialog = false
                                    showJsonFilePicker = true
                                }
                            ) {
                                Text(stringResource(Res.string.common_yes))
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = { showImportJsonConfirmDialog = false }
                            ) {
                                Text(stringResource(Res.string.common_cancel))
                            }
                        }
                    )
                }

                if (showImportZipConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { showImportZipConfirmDialog = false },
                        title = { Text(stringResource(Res.string.import_dialog_title)) },
                        text = { Text(stringResource(Res.string.import_dialog_message)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showImportZipConfirmDialog = false
                                    showZipFilePicker = true
                                }
                            ) {
                                Text(stringResource(Res.string.common_yes))
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = { showImportZipConfirmDialog = false }
                            ) {
                                Text(stringResource(Res.string.common_cancel))
                            }
                        }
                    )
                }


                if (showJsonFilePicker) {
                    FilePicker(show = true) { fileContent ->
                        showJsonFilePicker = false
                        if (fileContent != null) {
                            onImport(fileContent)
                        }
                    }
                }
                
                if (showZipFilePicker) {
                    FilePickerForZip(show = true) { fileContent ->
                        showZipFilePicker = false
                        if (fileContent != null) {
                            onImportZip(fileContent)
                        }
                    }
                }
            }
        }
    }
}
