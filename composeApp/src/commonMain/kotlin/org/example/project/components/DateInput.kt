package org.example.project.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import org.example.project.getCurrentTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInput(
    label: String,
    date: String?,
    onDateChange: (String?) -> Unit
) {
    val (initialYear, initialMonth, initialDay) = remember(date) {
        date?.split("-")?.let {
            if (it.size == 3) {
                Triple(it[0].toIntOrNull(), it[1].toIntOrNull(), it[2].toIntOrNull())
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
            if (parts.size == 3) {
                val year = parts[0].toIntOrNull()
                val month = parts[1].toIntOrNull()
                val day = parts[2].toIntOrNull()
                if (year != selectedYear || month != selectedMonth || day != selectedDay) {
                    selectedYear = year
                    selectedMonth = month
                    selectedDay = day
                }
            }
        }
    }

    var expandedYear by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }
    var expandedDay by remember { mutableStateOf(false) }

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
        if (y != null && m != null && d != null) {
            val newDate = "$y-${m.toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"
            if (date != newDate) {
                onDateChange(newDate)
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = " ", // This space keeps the label floated
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                if (date != null) {
                    IconButton(onClick = { onDateChange(null) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear date")
                    }
                }
            }
        )
        Row(
            modifier = Modifier
                //.matchParentSize()
                .padding(top = 8.dp, end = 52.dp),
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
                    placeholder = { Text("Year") },
                    suffix = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(24.dp).offset(x = -15.dp))},
                    //trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                    modifier = Modifier.menuAnchor(),
                    colors = textFieldColors
                )
                ExposedDropdownMenu(
                    expanded = expandedYear,
                    onDismissRequest = { expandedYear = false }
                ) {
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
                    placeholder = { Text("Month") },
                    suffix = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(24.dp).offset(x = -15.dp))},
                    //trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                    modifier = Modifier.menuAnchor(),
                    colors = textFieldColors
                )
                ExposedDropdownMenu(
                    expanded = expandedMonth,
                    onDismissRequest = { expandedMonth = false }
                ) {
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
                    placeholder = { Text("Day") },
                    suffix = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(24.dp).offset(x = -15.dp))},
                    modifier = Modifier.menuAnchor(),
                    colors = textFieldColors
                )
                ExposedDropdownMenu(
                    expanded = expandedDay,
                    onDismissRequest = { expandedDay = false }
                ) {
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
        }
    }
}
