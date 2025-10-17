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
import openyarnstash.composeapp.generated.resources.*
import org.example.project.components.SelectAllOutlinedTextField
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.math.round

// Helper function to normalize integer input fields
fun normalizeIntInput(input: String): String {
    val filtered = input.filter { it.isDigit() }
    return if (filtered.startsWith("0") && filtered.length > 1) {
        filtered.substring(1)
    } else {
        filtered
    }
}

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
    var colorLot by remember { mutableStateOf(initial?.colorLot ?: "") }

    var gramsPerBallText by remember { mutableStateOf(initial?.gramsPerBall?.toString() ?: "") }
    var metersPerBallText by remember { mutableStateOf(initial?.metersPerBall?.toString() ?: "") }
    var amountState by remember(initial) { mutableStateOf(initial?.amount?.toString()?.takeIf { it != "0" } ?: "") }
    var numberOfBallsText by remember { mutableStateOf("1") }

    var dateAddedState by remember { mutableStateOf(initial?.dateAdded ?: getCurrentTimestamp()) }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    val isUsedInProjects = usagesForYarn.isNotEmpty()
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val hasChanges by remember(
        name,
        color,
        brand,
        colorLot,
        gramsPerBallText,
        metersPerBallText,
        amountState,
        dateAddedState,
        notes
    ) {
        derivedStateOf {
            if (initial == null) {
                name.isNotEmpty() || color.isNotEmpty() || brand.isNotEmpty() ||
                        colorLot.isNotEmpty() || gramsPerBallText.isNotEmpty() || metersPerBallText.isNotEmpty() ||
                        amountState.isNotEmpty() ||
                        dateAddedState != getCurrentTimestamp() || notes.isNotEmpty()
            } else {
                name != initial.name ||
                        color != (initial.color ?: "") ||
                        brand != (initial.brand ?: "") ||
                        colorLot != (initial.colorLot ?: "") ||
                        gramsPerBallText != (initial.gramsPerBall?.toString() ?: "") ||
                        metersPerBallText != (initial.metersPerBall?.toString() ?: "") ||
                        amountState != (initial.amount.toString().takeIf { it != "0" } ?: "") ||
                        dateAddedState != initial.dateAdded ||
                        notes != (initial.notes ?: "")
            }
        }
    }

    val saveAction = {
        val enteredAmount = amountState.toIntOrNull() ?: 0
        val finalAmountToSave = max(enteredAmount, totalUsedAmount)

        val yarn = (initial ?: Yarn(id = -1, name = "", amount = 0, dateAdded = getCurrentTimestamp()))
            .copy(
                name = name,
                brand = brand.ifBlank { null },
                color = color.ifBlank { null },
                colorLot = colorLot.ifBlank { null },
                amount = finalAmountToSave,
                gramsPerBall = gramsPerBallText.toIntOrNull(),
                metersPerBall = metersPerBallText.toIntOrNull(),
                dateAdded = dateAddedState,
                notes = notes.ifBlank { null }
            )
        onSave(yarn)
    }

    val backAction = {
        if (hasChanges) {
            showUnsavedDialog = true
        } else {
            onCancel()
        }
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
                        onCancel()
                    }) {
                        Text(stringResource(Res.string.common_no))
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        saveAction()
                        showUnsavedDialog = false
                    }) {
                        Text(stringResource(Res.string.common_yes))
                    }
                }
            },
            dismissButton = null
        )
    }

    LaunchedEffect(initial) {
        if (initial != null) {
            val gpb = initial.gramsPerBall
            val amount = initial.amount
            if (gpb != null && gpb > 0 && amount > 0) {
                val balls = amount.toDouble() / gpb.toDouble()
                numberOfBallsText = if (balls == balls.toInt().toDouble()) {
                    balls.toInt().toString()
                } else {
                    (round(balls * 100) / 100.0).toString().trimEnd('0').trimEnd('.')
                }
            } else if (gpb != null && gpb > 0 && amount == 0) {
                numberOfBallsText = "0"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initial == null) stringResource(Res.string.yarn_form_new) else stringResource(Res.string.yarn_form_edit)) },
                navigationIcon = {
                    IconButton(onClick = backAction) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back))
                    }
                }
            )
        },
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
            SelectAllOutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text(stringResource(Res.string.yarn_label_brand)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(Res.string.yarn_label_name)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(value = color, onValueChange = { color = it }, label = { Text(stringResource(Res.string.yarn_label_color)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(value = colorLot, onValueChange = { colorLot = it }, label = { Text(stringResource(Res.string.yarn_label_color_lot)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            SelectAllOutlinedTextField(
                value = gramsPerBallText,
                onValueChange = { newValue ->
                    val normalized = normalizeIntInput(newValue)
                    gramsPerBallText = normalized
                    val gpb = normalized.toIntOrNull()
                    val balls = numberOfBallsText.replace(",", ".").toDoubleOrNull()
                    if (gpb != null && balls != null && gpb > 0 && balls > 0) {
                        amountState = round(gpb * balls).toInt().toString()
                    }
                },
                label = { Text(stringResource(Res.string.yarn_label_grams_per_ball)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(
                value = metersPerBallText,
                onValueChange = { metersPerBallText = normalizeIntInput(it) },
                label = { Text(stringResource(Res.string.yarn_label_meters_per_ball)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            SelectAllOutlinedTextField(
                value = numberOfBallsText,
                onValueChange = { newValue ->
                    val normalized = newValue.replace(",", ".")
                    if (normalized.isEmpty() || normalized.toDoubleOrNull() != null || (normalized.endsWith(".") && normalized.count { it == '.' } == 1)) {
                        numberOfBallsText = normalized
                        val balls = normalized.toDoubleOrNull()
                        val gpb = gramsPerBallText.toIntOrNull()
                        if (gpb != null && balls != null && gpb > 0) { // Allow balls > 0
                            amountState = round(gpb * balls).toInt().toString()
                        }
                    }
                },
                label = { Text(stringResource(Res.string.yarn_label_number_of_balls)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                SelectAllOutlinedTextField(
                    value = amountState,
                    onValueChange = { newValue ->
                        val normalized = normalizeIntInput(newValue)
                        amountState = normalized
                        val amount = normalized.toIntOrNull()
                        val gpb = gramsPerBallText.toIntOrNull()
                        if (gpb != null && amount != null && gpb > 0) { // Allow amount >= 0
                            val balls = amount.toDouble() / gpb.toDouble()
                            numberOfBallsText = if (balls == balls.toInt().toDouble()) {
                                balls.toInt().toString()
                            } else {
                                (round(balls * 100) / 100.0).toString().trimEnd('0').trimEnd('.')
                            }
                        } else if (amount == 0) {
                            numberOfBallsText = "0"
                        }
                    },
                    label = { Text(stringResource(Res.string.yarn_label_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    IconButton(onClick = {
                        val newValue = (amountState.toIntOrNull() ?: 0) + 1
                        amountState = newValue.toString()
                    }, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.KeyboardArrowUp, "Increment") }
                    IconButton(onClick = {
                        val decremented = (amountState.toIntOrNull() ?: 0) - 1
                        amountState = max(decremented, totalUsedAmount).toString().takeIf { it != "0" } ?: ""
                    }, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.KeyboardArrowDown, "Decrement") }
                }
            }
            Spacer(Modifier.height(8.dp))

            SelectAllOutlinedTextField(value = dateAddedState, onValueChange = { dateAddedState = it }, label = { Text(stringResource(Res.string.yarn_label_date_added)) }, supportingText = { Text(stringResource(Res.string.date_format_hint_yarn_added)) }, modifier = Modifier.fillMaxWidth(), readOnly = true)
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text(stringResource(Res.string.yarn_label_notes)) }, singleLine = false, minLines = 3, modifier = Modifier.fillMaxWidth())

            if (initial != null) {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(Res.string.usage_projects_title), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (usagesForYarn.isEmpty()) {
                    Text(stringResource(Res.string.yarn_form_no_projects_assigned))
                } else {
                    usagesForYarn.forEach { usage ->
                        Text("- ${projectNameById(usage.projectId)}: ${usage.amount} g")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = backAction) { Text(stringResource(Res.string.common_cancel)) }
                Row {
                    if (initial != null) {
                        if (isUsedInProjects) {
                            TextButton(onClick = { onSetRemainingToZero(initial.id, totalUsedAmount) }) { Text(stringResource(Res.string.yarn_form_button_set_remaining_to_zero)) }
                        } else {
                            TextButton(onClick = { onDelete(initial.id) }) { Text(stringResource(Res.string.common_delete)) }
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Button(onClick = saveAction) { Text(stringResource(Res.string.common_save)) }
                }
            }
        }
    }
}
