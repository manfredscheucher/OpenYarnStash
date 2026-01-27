package org.example.project.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import openyarnstash.composeapp.generated.resources.Res
import openyarnstash.composeapp.generated.resources.date_input_day
import openyarnstash.composeapp.generated.resources.date_input_month
import openyarnstash.composeapp.generated.resources.date_input_year
import org.example.project.LogLevel
import org.example.project.Logger
import org.example.project.getCurrentTimestamp
import org.jetbrains.compose.resources.stringResource

@Composable
private fun DropdownArrowIcon() {
    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(24.dp).offset(x = -15.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInput(
    label: String,
    date: String?,
    onDateChange: (String?) -> Unit
) {
    val (initialYear, initialMonth, initialDay) = remember(date) {
        date?.split("-")?.let {
            if (it.size >= 1) {
                val year = it.getOrNull(0)?.toIntOrNull()
                val month = it.getOrNull(1)?.toIntOrNull()
                val day = it.getOrNull(2)?.toIntOrNull()
                Triple(year, month, day)
            } else {
                Triple(null, null, null)
            }
        } ?: Triple(null, null, null)
    }

    var selectedYear by remember { mutableStateOf(initialYear) }
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    var selectedDay by remember { mutableStateOf(initialDay) }

    LaunchedEffect(date) {
        if (date == null) {
            selectedYear = null
            selectedMonth = null
            selectedDay = null
        } else {
            val parts = date.split("-")
            val year = parts.getOrNull(0)?.toIntOrNull()
            val month = parts.getOrNull(1)?.toIntOrNull()
            val day = parts.getOrNull(2)?.toIntOrNull()
            if (year != selectedYear || month != selectedMonth || day != selectedDay) {
                selectedYear = year
                selectedMonth = month
                selectedDay = day
            }
        }
    }

    var expandedYear by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }
    var expandedDay by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val currentYear = getCurrentTimestamp().substring(0, 4).toInt()
    val years = (currentYear - 10..currentYear + 10).toList()
    val months = (1..12).toList()

    val daysInMonth = remember(selectedYear, selectedMonth) {
        if (selectedYear != null && selectedMonth != null) {
            try {
                val startOfMonth = LocalDate(selectedYear!!, selectedMonth!!, 1)
                val startOfNextMonth = startOfMonth.plus(1, DateTimeUnit.MONTH)
                startOfMonth.daysUntil(startOfNextMonth)
            } catch (e: Exception) {
                scope.launch {
                    Logger.log(LogLevel.ERROR, "Failed to calculate days in month", e)
                }
                31
            }
        } else {
            31
        }
    }
    val days = (1..daysInMonth).toList()

    LaunchedEffect(daysInMonth) {
        if (selectedDay != null && selectedDay!! > daysInMonth) {
            selectedDay = null
        }
    }

    LaunchedEffect(selectedYear, selectedMonth, selectedDay) {
        val y = selectedYear
        val m = selectedMonth
        val d = selectedDay

        val newDate = when {
            y != null && m != null && d != null -> "$y-${m.toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"
            y != null && m != null -> "$y-${m.toString().padStart(2, '0')}"
            y != null -> "$y"
            else -> null
        }

        if (date != newDate) {
            onDateChange(newDate)
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = " ", // This space keeps the label floated
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            label = { Text(label) },
        )
        Row(
            modifier = Modifier
                //.matchParentSize()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            //horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val textFieldColors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            )

            // Year
            ExposedDropdownMenuBox(
                expanded = expandedYear,
                onExpandedChange = { expandedYear = !expandedYear },
                modifier = Modifier.width(120.dp)
            ) {
                TextField(
                    value = selectedYear?.toString() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text(stringResource(Res.string.date_input_year)) },
                    suffix = { DropdownArrowIcon() },
                    modifier = Modifier.menuAnchor(),
                    colors = textFieldColors
                )
                ExposedDropdownMenu(
                    expanded = expandedYear,
                    onDismissRequest = { expandedYear = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("-") },
                        onClick = {
                            selectedYear = null
                            expandedYear = false
                        }
                    )
                    years.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.toString()) },
                            onClick = {
                                selectedYear = selectionOption
                                expandedYear = false
                            }
                        )
                    }
                }
            }

            // Month
            ExposedDropdownMenuBox(
                expanded = expandedMonth,
                onExpandedChange = { expandedMonth = !expandedMonth },
                modifier = Modifier.width(120.dp)
            ) {
                TextField(
                    value = selectedMonth?.toString()?.padStart(2, '0') ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text(stringResource(Res.string.date_input_month)) },
                    suffix = { DropdownArrowIcon() },
                    modifier = Modifier.menuAnchor(),
                    colors = textFieldColors
                )
                ExposedDropdownMenu(
                    expanded = expandedMonth,
                    onDismissRequest = { expandedMonth = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("-") },
                        onClick = {
                            selectedMonth = null
                            expandedMonth = false
                        }
                    )
                    months.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.toString().padStart(2, '0')) },
                            onClick = {
                                selectedMonth = selectionOption
                                expandedMonth = false
                            }
                        )
                    }
                }
            }

            // Day
            ExposedDropdownMenuBox(
                expanded = expandedDay,
                onExpandedChange = { expandedDay = !expandedDay },
                modifier = Modifier.width(120.dp)
            ) {
                TextField(
                    value = selectedDay?.toString()?.padStart(2, '0') ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text(stringResource(Res.string.date_input_day)) },
                    suffix = { DropdownArrowIcon() },
                    modifier = Modifier.menuAnchor(),
                    colors = textFieldColors
                )
                ExposedDropdownMenu(
                    expanded = expandedDay,
                    onDismissRequest = { expandedDay = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("-") },
                        onClick = {
                            selectedDay = null
                            expandedDay = false
                        }
                    )
                    days.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.toString().padStart(2, '0')) },
                            onClick = {
                                selectedDay = selectionOption
                                expandedDay = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Today button
            IconButton(
                onClick = {
                    val today = getCurrentTimestamp().substring(0, 10) // YYYY-MM-DD
                    val parts = today.split("-")
                    selectedYear = parts[0].toIntOrNull()
                    selectedMonth = parts[1].toIntOrNull()
                    selectedDay = parts[2].toIntOrNull()
                }
            ) {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = "Today")
            }
        }
    }
}
