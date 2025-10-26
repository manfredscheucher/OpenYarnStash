package org.example.project.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import java.time.LocalDate
import java.time.YearMonth

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
            date.split("-").let {
                if (it.size == 3) {
                    selectedYear = it[0].toIntOrNull()
                    selectedMonth = it[1].toIntOrNull()
                    selectedDay = it[2].toIntOrNull()
                }
            }
        }
    }

    var expandedYear by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }
    var expandedDay by remember { mutableStateOf(false) }

    val currentYear = LocalDate.now().year
    val years = (currentYear - 10..currentYear + 10).toList()
    val months = (1..12).toList()

    val daysInMonth = remember(selectedYear, selectedMonth) {
        if (selectedYear != null && selectedMonth != null) {
            try {
                YearMonth.of(selectedYear!!, selectedMonth!!).lengthOfMonth()
            } catch (e: Exception) {
                31
            }
        } else {
            31
        }
    }
    val days = (1..daysInMonth).toList()

    if (selectedDay != null && selectedDay!! > daysInMonth) {
        selectedDay = null
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
        } else if (date != null) {
            onDateChange(null)
        }
    }
    Column(modifier = Modifier
        .fillMaxWidth()
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = MaterialTheme.shapes.extraSmall
        )
        .padding(horizontal = 12.dp, vertical = 4.dp),
        //verticalAlignment = Alignment.CenterVertically,
        //horizontalArrangement = Arrangement.spacedBy(4.dp)
        )
    {
        Text(label)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            //.border(
                //width = 1.dp,
                //color = MaterialTheme.colorScheme.outline,
                //shape = MaterialTheme.shapes.extraSmall
            //)
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
            modifier = Modifier.weight(1.5f)
        ) {
            TextField(
                value = selectedYear?.toString() ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Year") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
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
            modifier = Modifier.weight(1.2f)
        ) {
            TextField(
                value = selectedMonth?.toString() ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Month") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                modifier = Modifier.menuAnchor(),
                colors = textFieldColors
            )
            ExposedDropdownMenu(
                expanded = expandedMonth,
                onDismissRequest = { expandedMonth = false }
            ) {
                months.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption.toString()) },
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
            modifier = Modifier.weight(1.2f)
        ) {
            TextField(
                value = selectedDay?.toString() ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Day") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDay) },
                modifier = Modifier.menuAnchor(),
                colors = textFieldColors
            )
            ExposedDropdownMenu(
                expanded = expandedDay,
                onDismissRequest = { expandedDay = false }
            ) {
                days.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption.toString()) },
                        onClick = {
                            selectedDay = selectionOption
                            expandedDay = false
                        }
                    )
                }
            }
        }

        if (date != null) {
            IconButton(onClick = { onDateChange(null) }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear date")
            }
        }
    }
    }
}
