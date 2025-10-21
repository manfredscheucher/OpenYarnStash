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
    onBack: () -> Unit,
    onDelete: (Int) -> Unit,
    onSave: (Yarn) -> Unit,
    onSetRemainingToZero: (yarnId: Int, newAmount: Int) -> Unit
) {
    val totalUsedAmount = usagesForYarn.sumOf { it.amount }

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var color by remember { mutableStateOf(initial?.color ?: "") }
    var colorCode by remember { mutableStateOf(initial?.colorCode ?: "") }
    var brand by remember { mutableStateOf(initial?.brand ?: "") }
    var blend by remember { mutableStateOf(initial?.blend ?: "") }
    var dyeLot by remember { mutableStateOf(initial?.dyeLot ?: "") }

    var weightPerSkeinText by remember { mutableStateOf(initial?.weightPerSkein?.toString() ?: "") }
    var meteragePerSkeinText by remember { mutableStateOf(initial?.meteragePerSkein?.toString() ?: "") }
    var amountText by remember(initial) { mutableStateOf(initial?.amount?.toString()?.takeIf { it != "0" } ?: "") }
    var numberOfBallsText by remember { mutableStateOf("1") }

    val modifiedState by remember { mutableStateOf(initial?.modified ?: getCurrentTimestamp()) }
    var added by remember { mutableStateOf(initial?.added ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    val isUsedInProjects = usagesForYarn.isNotEmpty()
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val hasChanges by remember(
        name,
        color,
        colorCode,
        brand,
        blend,
        dyeLot,
        weightPerSkeinText,
        meteragePerSkeinText,
        amountText,
        added,
        notes
    ) {
        derivedStateOf {
            if (initial == null) {
                name.isNotEmpty() || color.isNotEmpty() || colorCode.isNotEmpty() || brand.isNotEmpty() ||
                        blend.isNotEmpty() ||
                        dyeLot.isNotEmpty() || weightPerSkeinText.isNotEmpty() || meteragePerSkeinText.isNotEmpty() ||
                        amountText.isNotEmpty() ||
                        added.isNotEmpty() || notes.isNotEmpty()
            } else {
                name != initial.name ||
                        color != (initial.color ?: "") ||
                        colorCode != (initial.colorCode ?: "") ||
                        brand != (initial.brand ?: "") ||
                        blend != (initial.blend ?: "") ||
                        dyeLot != (initial.dyeLot ?: "") ||
                        weightPerSkeinText != (initial.weightPerSkein?.toString() ?: "") ||
                        meteragePerSkeinText != (initial.meteragePerSkein?.toString() ?: "") ||
                        amountText != (initial.amount.toString().takeIf { it != "0" } ?: "") ||
                        added != (initial.added ?: "") ||
                        notes != (initial.notes ?: "")
            }
        }
    }

    val saveAction = {
        val enteredAmount = amountText.toIntOrNull() ?: 0
        val finalAmountToSave = max(enteredAmount, totalUsedAmount)

        val yarn = (initial ?: Yarn(id = -1, name = "", amount = 0, modified = getCurrentTimestamp()))
            .copy(
                name = name,
                brand = brand.ifBlank { null },
                color = color.ifBlank { null },
                colorCode = colorCode.ifBlank { null },
                blend = blend.ifBlank { null },
                dyeLot = dyeLot.ifBlank { null },
                amount = finalAmountToSave,
                weightPerSkein = weightPerSkeinText.toIntOrNull(),
                meteragePerSkein = meteragePerSkeinText.toIntOrNull(),
                modified = getCurrentTimestamp(),
                added = normalizeDateString(added),
                notes = notes.ifBlank { null }
            )
        onSave(yarn)
    }

    val backAction = {
        if (hasChanges) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
    }

    AppBackHandler {
        backAction()
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
            val gpb = initial.weightPerSkein
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
            SelectAllOutlinedTextField(value = colorCode, onValueChange = { colorCode = it }, label = { Text(stringResource(Res.string.yarn_label_color_code)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(value = blend, onValueChange = { blend = it }, label = { Text(stringResource(Res.string.yarn_label_blend)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(value = dyeLot, onValueChange = { dyeLot = it }, label = { Text(stringResource(Res.string.yarn_label_dye_lot)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            SelectAllOutlinedTextField(
                value = weightPerSkeinText,
                onValueChange = { newValue ->
                    val normalized = normalizeIntInput(newValue)
                    weightPerSkeinText = normalized
                    val gpb = normalized.toIntOrNull()
                    val balls = numberOfBallsText.replace(",", ".").toDoubleOrNull()
                    if (gpb != null && balls != null && gpb > 0 && balls > 0) {
                        amountText = round(gpb * balls).toInt().toString()
                    }
                },
                label = { Text(stringResource(Res.string.yarn_label_weight_per_skein)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(
                value = meteragePerSkeinText,
                onValueChange = { meteragePerSkeinText = normalizeIntInput(it) },
                label = { Text(stringResource(Res.string.yarn_label_meterage_per_skein)) },
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
                        val gpb = weightPerSkeinText.toIntOrNull()
                        if (gpb != null && balls != null && gpb > 0) { // Allow balls > 0
                            amountText = round(gpb * balls).toInt().toString()
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
                    value = amountText,
                    onValueChange = { newValue ->
                        val normalized = normalizeIntInput(newValue)
                        amountText = normalized
                        val amount = normalized.toIntOrNull()
                        val gpb = weightPerSkeinText.toIntOrNull()
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
                        val newValue = (amountText.toIntOrNull() ?: 0) + 1
                        amountText = newValue.toString()
                    }, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.KeyboardArrowUp, "Increment") }
                    IconButton(onClick = {
                        val decremented = (amountText.toIntOrNull() ?: 0) - 1
                        amountText = max(decremented, totalUsedAmount).toString().takeIf { it != "0" } ?: ""
                    }, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.KeyboardArrowDown, "Decrement") }
                }
            }
            Spacer(Modifier.height(8.dp))

            SelectAllOutlinedTextField(
                value = added,
                onValueChange = { added = it },
                label = { Text(stringResource(Res.string.yarn_label_modified)) },
                supportingText = { Text(stringResource(Res.string.date_format_hint_modified)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(stringResource(Res.string.yarn_item_label_modified, modifiedState))
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
