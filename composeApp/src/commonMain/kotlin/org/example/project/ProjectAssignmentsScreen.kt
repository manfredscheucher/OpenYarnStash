package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import knittingappmultiplatt.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectAssignmentsScreen(
    projectName: String,
    allYarns: List<Yarn>,
    initialAssignments: Map<Int, Int>,
    getAvailableAmountForYarn: (yarnId: Int) -> Int, // Already accounts for current project's usage being editable
    onSave: (updatedAssignments: Map<Int, Int>) -> Unit,
    onCancel: () -> Unit
) {
    var currentAssignments by remember { mutableStateOf(initialAssignments.toMutableMap()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.project_assignments_title, projectName)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back))
                    }
                }
            )
        },
        bottomBar = {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.End // Save button to the right
            ) {
                TextButton(onClick = onCancel) { Text(stringResource(Res.string.common_cancel)) }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    val finalAssignments = currentAssignments.filterValues { it > 0 } // Remove zero amounts
                    onSave(finalAssignments)
                }) { Text(stringResource(Res.string.common_save)) }
            }
        }
    ) { paddingValues ->
        if (allYarns.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Potentially a new string resource for "No yarns available to assign"
                Text(stringResource(Res.string.yarn_list_empty)) // Re-using for now
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues) // Apply padding from Scaffold
                    .fillMaxSize()
                    .imePadding() // Handles keyboard overlap
                    .navigationBarsPadding(), // Handles navigation bar overlap
                contentPadding = PaddingValues(16.dp) // Inner padding for content list
            ) {
                items(allYarns, key = { it.id }) { yarn ->
                    val assignedAmount = currentAssignments[yarn.id] ?: 0
                    // availableAmountForAssignment is the max this yarn can be assigned in this project,
                    // considering its total amount and what's used in *other* projects.
                    // The amount already assigned in *this* project is effectively "given back" by getAvailableAmountForYarn.
                    val availableAmountForAssignment = getAvailableAmountForYarn(yarn.id)
                    val totalAvailableForThisAssignment = availableAmountForAssignment + assignedAmount

                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("${yarn.name} (${yarn.color ?: "?"}) - Total: ${yarn.amount}g", style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(Res.string.usage_available, totalAvailableForThisAssignment) + " for this project")
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = assignedAmount.toString(),
                            onValueChange = { textValue ->
                                val rawValue = textValue.filter { it.isDigit() }
                                val numericValue = rawValue.toIntOrNull() ?: 0
                                // Clamp the value against what's available for *this specific assignment slot*
                                val clampedValue = numericValue.coerceIn(0, totalAvailableForThisAssignment)
                                currentAssignments = currentAssignments.toMutableMap().apply {
                                    this[yarn.id] = clampedValue
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
            }
        }
    }
}
