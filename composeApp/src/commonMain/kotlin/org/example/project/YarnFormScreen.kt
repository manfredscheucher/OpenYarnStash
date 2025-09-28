package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import knittingappmultiplatt.composeapp.generated.resources.*
import kotlin.math.max

// Helper function for date normalization
fun normalizeDateString(input: String): String? {
    val trimmed = input.trim()
    if (trimmed.isBlank()) return null

    // Regex for YYYY
    val yyyyRegex = "^\\d{4}$".toRegex()
    // Regex for YYYY-MM
    val yyyyMmRegex = "^\\d{4}-(0[1-9]|1[0-2])$".toRegex()
    // Regex for YYYY-MM-DD
    val yyyyMmDdRegex = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$".toRegex()

    return when {
        yyyyRegex.matches(trimmed) -> "$trimmed-01-01"
        yyyyMmRegex.matches(trimmed) -> "$trimmed-01"
        yyyyMmDdRegex.matches(trimmed) -> trimmed
        else -> null // Invalid format
    }
}

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
    var brand by remember { mutableStateOf(initial?.brand ?: "") }
    var amountState by remember(initial, totalUsedAmount) {
        val initialDisplayAmount = initial?.let {
            max(it.amount, totalUsedAmount)
        } ?: 0
        mutableStateOf(initialDisplayAmount.toString())
    }
    var url by remember { mutableStateOf(initial?.url ?: "") }
    var dateAddedState by remember { mutableStateOf(initial?.dateAdded ?: "") } // Changed from 'date' to 'dateAddedState'

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
                            enteredAmountInTextField 
                        }
                        val normalizedDate = normalizeDateString(dateAddedState) // Use dateAddedState here

                        val yarn = (initial ?: Yarn(id = -1, name = "", amount = 0))
                            .copy(
                                name = name,
                                color = color.ifBlank { null },
                                brand = brand.ifBlank { null }, 
                                amount = finalAmountToSave, 
                                url = url.ifBlank { null },
                                dateAdded = normalizedDate // Save to dateAdded field
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
            OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text(stringResource(Res.string.yarn_label_brand)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = amountState,
                    onValueChange = { inputValue ->
                        val filteredValue = inputValue.filter { it.isDigit() }
                        if (initial != null) {
                            val numericValue = filteredValue.toIntOrNull()
                            if (numericValue != null) {
                                if (numericValue < totalUsedAmount) {
                                    amountState = totalUsedAmount.toString()
                                } else {
                                    amountState = filteredValue
                                }
                            } else {
                                amountState = if (totalUsedAmount > 0) totalUsedAmount.toString() else ""
                            }
                        } else {
                            amountState = filteredValue
                        }
                    },
                    label = { Text(stringResource(Res.string.yarn_label_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    IconButton(onClick = {
                        val currentNumericValue = amountState.toIntOrNull() ?: 0
                        amountState = (currentNumericValue + 1).toString()
                    }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Increment") 
                    }
                    IconButton(onClick = {
                        val currentNumericValue = amountState.toIntOrNull() ?: 0
                        val decrementedValue = currentNumericValue - 1
                        val minimumAmount = if (initial != null) totalUsedAmount else 0
                        amountState = max(decrementedValue, minimumAmount).toString()
                    }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Decrement") 
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text(stringResource(Res.string.yarn_label_url)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = dateAddedState, // Use dateAddedState
                onValueChange = { dateAddedState = it }, // Update dateAddedState
                label = { Text(stringResource(Res.string.yarn_label_date_added)) }, // Use new string resource
                supportingText = { Text(stringResource(Res.string.date_format_hint_yarn_added)) }, // Use new string resource
                modifier = Modifier.fillMaxWidth()
            )

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
