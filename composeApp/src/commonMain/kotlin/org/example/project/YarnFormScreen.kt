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
import kotlin.math.round

// Helper function for date normalization (remains unchanged)
fun normalizeDateString(input: String): String? {
    val trimmed = input.trim()
    if (trimmed.isBlank()) return null
    val yyyyRegex = "^\\d{4}$".toRegex()
    val yyyyMmRegex = "^\\d{4}-(0[1-9]|1[0-2])$".toRegex()
    val yyyyMmDdRegex = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$".toRegex()
    return when {
        yyyyRegex.matches(trimmed) -> "$trimmed-01-01"
        yyyyMmRegex.matches(trimmed) -> "$trimmed-01"
        yyyyMmDdRegex.matches(trimmed) -> trimmed
        else -> null
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

    var gramsPerBallText by remember { mutableStateOf(initial?.gramsPerBall?.toString() ?: "") }
    var metersPerBallText by remember { mutableStateOf(initial?.metersPerBall?.toString() ?: "") }
    var amountState by remember(initial, totalUsedAmount) {
        val initialDisplayAmount = initial?.let { max(it.amount, totalUsedAmount) } ?: 0
        mutableStateOf(initialDisplayAmount.toString())
    }
    var numberOfBallsText by remember { mutableStateOf("") }

    var url by remember { mutableStateOf(initial?.url ?: "") }
    var dateAddedState by remember { mutableStateOf(initial?.dateAdded ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    val isUsedInProjects = usagesForYarn.isNotEmpty()

    // Synchronize numberOfBallsText when amountState or gramsPerBallText changes
    LaunchedEffect(amountState, gramsPerBallText) {
        val currentAmountInt = amountState.toIntOrNull()
        val gpbInt = gramsPerBallText.toIntOrNull()

        if (currentAmountInt != null && gpbInt != null && gpbInt > 0) {
            val calculatedBalls = currentAmountInt.toDouble() / gpbInt
            // Format to avoid scientific notation and unnecessary decimals
            val formattedBalls = if (calculatedBalls == calculatedBalls.toInt().toDouble()) {
                calculatedBalls.toInt().toString()
            } else {
                String.format("%.2f", calculatedBalls).replace(",", ".")
            }
            if (numberOfBallsText != formattedBalls) {
                numberOfBallsText = formattedBalls
            }
        } else {
            if (numberOfBallsText != "") {
                numberOfBallsText = "" // Clear if gpb is invalid or amount is invalid
            }
        }
    }

    // Synchronize amountState when numberOfBallsText or gramsPerBallText changes
    LaunchedEffect(numberOfBallsText, gramsPerBallText) {
        val currentNumBallsDouble = numberOfBallsText.replace(",", ".").toDoubleOrNull()
        val gpbInt = gramsPerBallText.toIntOrNull()

        if (currentNumBallsDouble != null && gpbInt != null && gpbInt > 0) {
            val calculatedAmount = round(currentNumBallsDouble * gpbInt).toInt()
            val minimumAmount = if (initial != null) totalUsedAmount else 0
            val finalAmount = max(calculatedAmount, minimumAmount)
            if (amountState != finalAmount.toString()) {
                amountState = finalAmount.toString()
            }
        }
    }

    Scaffold(
        topBar = { /* ... TopAppBar ... */ },
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
                        val enteredAmountInTextField = amountState.toIntOrNull() ?: 0 // Final amount from amountState
                        val finalAmountToSave = if (initial != null) { 
                            max(enteredAmountInTextField, totalUsedAmount)
                        } else {
                            enteredAmountInTextField 
                        }
                        val normalizedDate = normalizeDateString(dateAddedState)

                        val yarn = (initial ?: Yarn(id = -1, name = "", amount = 0))
                            .copy(
                                name = name,
                                color = color.ifBlank { null },
                                brand = brand.ifBlank { null }, 
                                amount = finalAmountToSave, 
                                gramsPerBall = gramsPerBallText.toIntOrNull(),
                                metersPerBall = metersPerBallText.toIntOrNull(),
                                url = url.ifBlank { null },
                                dateAdded = normalizedDate,
                                notes = notes.ifBlank { null }
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

            OutlinedTextField(
                value = gramsPerBallText,
                onValueChange = { newValue -> gramsPerBallText = newValue.filter { it.isDigit() } },
                label = { Text(stringResource(Res.string.yarn_label_grams_per_ball)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = metersPerBallText,
                onValueChange = { newValue -> metersPerBallText = newValue.filter { it.isDigit() } },
                label = { Text(stringResource(Res.string.yarn_label_meters_per_ball)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = amountState,
                    onValueChange = { inputValue ->
                        val filteredValue = inputValue.filter { it.isDigit() }
                        if (initial != null) {
                            val numericValue = filteredValue.toIntOrNull()
                            if (numericValue != null) {
                                amountState = if (numericValue < totalUsedAmount) totalUsedAmount.toString() else filteredValue
                            } else {
                                amountState = if (totalUsedAmount > 0) totalUsedAmount.toString() else "" // Clear or set to min if invalid
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
                    }, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.KeyboardArrowUp, "Increment") }
                    IconButton(onClick = {
                        val currentNumericValue = amountState.toIntOrNull() ?: 0
                        val decrementedValue = currentNumericValue - 1
                        val minimumAmount = if (initial != null) totalUsedAmount else 0
                        amountState = max(decrementedValue, minimumAmount).toString()
                    }, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.KeyboardArrowDown, "Decrement") }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = numberOfBallsText,
                onValueChange = { newValue ->
                    val normalizedInput = newValue.replace(',', '.')
                    // Allow empty, or a valid start of a double (e.g. "1", "1.", "1.2", ".5")
                    if (normalizedInput.isEmpty() || normalizedInput.toDoubleOrNull() != null || (normalizedInput == "." && !numberOfBallsText.contains('.')) ) {
                         if (normalizedInput.count { it == '.' } <= 1) {
                            numberOfBallsText = normalizedInput
                         }
                    }
                },
                label = { Text(stringResource(Res.string.yarn_label_number_of_balls)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text(stringResource(Res.string.yarn_label_url)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = dateAddedState, onValueChange = { dateAddedState = it }, label = { Text(stringResource(Res.string.yarn_label_date_added)) }, supportingText = { Text(stringResource(Res.string.date_format_hint_yarn_added)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text(stringResource(Res.string.yarn_label_notes)) }, singleLine = false, minLines = 3, modifier = Modifier.fillMaxWidth())

            if (initial != null) { /* ... Usages section ... */ }
        }
    }
}

// Placeholder for TopAppBar and Usages section to keep the snippet focused, assuming they are complex and mostly unchanged.
// Actual implementation would include the full Scaffold content.
// TopAppBar should be: TopAppBar(title = { Text(if (initial == null) stringResource(Res.string.yarn_form_new) else stringResource(Res.string.yarn_form_edit)) }, navigationIcon = { IconButton(onClick = onCancel) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.common_back)) } })
// Usages section: if (initial != null) { Spacer(Modifier.height(16.dp)); Text(stringResource(Res.string.usage_projects_title), style = MaterialTheme.typography.titleMedium); /* ... rest of usage display ... */ } 
