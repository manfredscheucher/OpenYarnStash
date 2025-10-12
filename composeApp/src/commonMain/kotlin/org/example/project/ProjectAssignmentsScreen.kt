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
import openyarnstash.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectAssignmentsScreen(
    projectName: String,
    allYarns: List<Yarn>,
    initialAssignments: Map<Int, Int>,
    getAvailableAmountForYarn: (yarnId: Int) -> Int, // This is the max that can be assigned from the yarn's total
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
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .imePadding()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(allYarns, key = { it.id }) { yarn ->
                    val assignedAmountInTextField = currentAssignments[yarn.id] ?: 0
                    val maxAmountThisProjectCanTake = getAvailableAmountForYarn(yarn.id)

                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("${yarn.name} (${yarn.color ?: "?"}) - Gesamt im Stash: ${yarn.amount}g", style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(Res.string.usage_available, maxAmountThisProjectCanTake) + " für dieses Projekt maximal verfügbar")
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = assignedAmountInTextField.toString(),
                            onValueChange = { textValue ->
                                val rawValue = textValue.filter { it.isDigit() }
                                val numericValue = rawValue.toIntOrNull() ?: 0
                                val clampedValue = numericValue.coerceIn(0, maxAmountThisProjectCanTake)
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

                item {
                    Spacer(Modifier.height(24.dp))
                    Row(
                        Modifier.fillMaxWidth(),
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
            }
        }
    }
}
