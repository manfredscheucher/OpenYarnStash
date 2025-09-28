package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import knittingappmultiplatt.composeapp.generated.resources.*
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YarnFormScreen(
    initial: Yarn?,
    usagesForYarn: List<Usage>,
    projectNameById: (Int) -> String,
    onCancel: () -> Unit,
    onDelete: (Int) -> Unit,
    onSave: (Yarn) -> Unit,
    onSetRemainingToZero: (yarnId: Int, newAmount: Int) -> Unit
) {
    val totalUsedAmount = usagesForYarn.sumOf { it.amount }

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var color by remember { mutableStateOf(initial?.color ?: "") }
    var amountState by remember(initial, totalUsedAmount) {
        val initialDisplayAmount = initial?.let {
            max(it.amount, totalUsedAmount)
        } ?: 0
        mutableStateOf(initialDisplayAmount.toString())
    }
    var url by remember { mutableStateOf(initial?.url ?: "") }
    var date by remember { mutableStateOf(initial?.date ?: "") }

    val isUsedInProjects = usagesForYarn.isNotEmpty()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (initial == null) stringResource(Res.string.yarn_form_new) else stringResource(Res.string.yarn_form_edit))
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onCancel) { Text(stringResource(Res.string.common_cancel)) }
                Row {
                    if (initial != null) {
                        if (isUsedInProjects) {
                            TextButton(onClick = { 
                                onSetRemainingToZero(initial.id, totalUsedAmount)
                            }) { Text(stringResource(Res.string.yarn_form_button_set_remaining_to_zero)) }
                        } else {
                            TextButton(onClick = { onDelete(initial.id) }) { Text(stringResource(Res.string.common_delete)) }
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Button(onClick = {
                        val enteredAmountInTextField = amountState.filter { it.isDigit() }.toIntOrNull() ?: 0
                        val finalAmountToSave = if (initial != null) { 
                            max(enteredAmountInTextField, totalUsedAmount)
                        } else {
                            enteredAmountInTextField // For a brand new, unused yarn
                        }

                        val yarn = (initial ?: Yarn(id = -1, name = "", amount = 0))
                            .copy(
                                name = name,
                                color = color.ifBlank { null },
                                amount = finalAmountToSave, 
                                url = url.ifBlank { null },
                                date = date.ifBlank { null }
                            )
                        onSave(yarn)
                    }) { Text(stringResource(Res.string.common_save)) }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(Res.string.yarn_label_name)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text(stringResource(Res.string.yarn_label_color)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = amountState,
                onValueChange = { inputValue ->
                    val filteredValue = inputValue.filter { it.isDigit() }
                    if (initial != null) { // Editing an existing yarn, minimum applies
                        val numericValue = filteredValue.toIntOrNull()
                        if (numericValue != null) {
                            if (numericValue < totalUsedAmount) {
                                amountState = totalUsedAmount.toString() // Correct to minimum
                            } else {
                                amountState = filteredValue // Accept valid input >= minimum
                            }
                        } else { // Input is empty or not a number after filtering
                            // If field is cleared, and there's a positive minimum, reset to minimum.
                            // Otherwise, allow empty (which means 0 if totalUsedAmount is 0).
                            amountState = if (totalUsedAmount > 0) totalUsedAmount.toString() else ""
                        }
                    } else { // New yarn, no minimum from usages
                        amountState = filteredValue
                    }
                },
                label = { Text(stringResource(Res.string.yarn_label_amount)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text(stringResource(Res.string.yarn_label_url)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text(stringResource(Res.string.yarn_label_date)) }, modifier = Modifier.fillMaxWidth())

            if (initial != null) {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(Res.string.usage_projects_title), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                val currentAmountInTextField = amountState.toIntOrNull() ?: 0
                val effectiveAmountInStash = max(currentAmountInTextField, totalUsedAmount)
                val availableForNewAssignments = (effectiveAmountInStash - totalUsedAmount).coerceAtLeast(0)

                Text(stringResource(Res.string.usage_used, totalUsedAmount))
                Text(stringResource(Res.string.usage_available, availableForNewAssignments))
                Spacer(Modifier.height(8.dp))

                if (usagesForYarn.isEmpty()) {
                    Text(stringResource(Res.string.yarn_form_no_projects_assigned))
                } else {
                    usagesForYarn.forEach { u ->
                        Text("- ${projectNameById(u.projectId)}: ${u.amount} g")
                    }
                }
            }
        }
    }
}
