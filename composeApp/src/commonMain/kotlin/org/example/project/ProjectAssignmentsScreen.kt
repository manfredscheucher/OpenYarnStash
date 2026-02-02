package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import openyarnstash.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectAssignmentsScreen(
    projectName: String,
    allYarns: List<Yarn>,
    initialAssignments: Map<UInt, Int>,
    getAvailableAmountForYarn: (yarnId: UInt) -> Int, // This is the max that can be assigned from the yarn's total
    onSave: (updatedAssignments: Map<UInt, Int>) -> Unit,
    onBack: () -> Unit
) {
    var currentAssignments by remember { mutableStateOf(initialAssignments.toMutableMap()) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf("") }

    val hasChanges by remember(currentAssignments) {
        derivedStateOf { currentAssignments != initialAssignments }
    }

    val filteredYarns = remember(allYarns, filter) {
        if (filter.isBlank()) {
            allYarns
        } else {
            allYarns.filter { yarn ->
                Json.encodeToString(yarn).contains(filter, ignoreCase = true)
            }
        }
    }

    val sortedYarns = remember(filteredYarns, initialAssignments) {
        filteredYarns.sortedByDescending { yarn ->
            (initialAssignments[yarn.id] ?: 0) > 0
        }
    }

    val backAction = {
        if (hasChanges) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
    }

    BackButtonHandler {
        backAction()
    }

    val saveAction = { callback: (() -> Unit)? ->
        val finalAssignments = currentAssignments.filterValues { it > 0 } // Remove zero amounts
        onSave(finalAssignments)
        callback?.invoke()
    }

    val saveAndGoBack = {
        saveAction { onBack() }
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text(stringResource(Res.string.form_unsaved_changes_title)) },
            text = { Text(stringResource(Res.string.form_unsaved_changes_message)) },
            confirmButton = {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showUnsavedDialog = false }) {
                        Text(stringResource(Res.string.common_stay))
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        showUnsavedDialog = false
                        onBack()
                    }) {
                        Text(stringResource(Res.string.common_no))
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        showUnsavedDialog = false
                        saveAction { onBack() }
                    }) {
                        Text(stringResource(Res.string.common_yes))
                    }
                }
            },
            dismissButton = null
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.project_assignments_title, projectName)) },
                navigationIcon = {
                    IconButton(onClick = backAction) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (allYarns.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(stringResource(Res.string.yarn_list_empty)) // Re-using for now, consider a specific string
            }
        } else {
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                OutlinedTextField(
                    value = filter,
                    onValueChange = { filter = it },
                    label = { Text("Filter") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    singleLine = true
                )

                val state = rememberLazyListState()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .navigationBarsPadding(),
                    state = state,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(sortedYarns, key = { it.id.toLong() }) { yarn ->
                        val assignedAmount = currentAssignments[yarn.id]
                        val maxAmountThisProjectCanTake = getAvailableAmountForYarn(yarn.id)

                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Text(
                                "${yarn.name} (${yarn.color ?: "?"}) - Gesamt im Stash: ${yarn.amount}g",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(stringResource(Res.string.usage_available, maxAmountThisProjectCanTake) + " für dieses Projekt maximal verfügbar")
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = assignedAmount?.toString() ?: "",
                                onValueChange = { textValue ->
                                    val numericValue = textValue.toIntOrNull()
                                    val clampedValue = numericValue?.coerceIn(0, maxAmountThisProjectCanTake)
                                    currentAssignments = currentAssignments.toMutableMap().apply {
                                        if (clampedValue != null) {
                                            this[yarn.id] = clampedValue
                                        } else {
                                            remove(yarn.id)
                                        }
                                    }
                                },
                                label = { Text(stringResource(Res.string.usage_amount_label)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Divider(Modifier.padding(top = 16.dp, bottom = 8.dp))
                        }
                    }

                    item {
                        Spacer(Modifier.height(24.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End // Save button to the right
                        ) {
                            TextButton(onClick = backAction) { Text(stringResource(Res.string.common_cancel)) }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = saveAndGoBack) { Text(stringResource(Res.string.common_save)) }
                        }
                    }
                }
            }
        }
    }
}
