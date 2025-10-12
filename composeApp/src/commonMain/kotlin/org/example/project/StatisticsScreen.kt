package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import knittingappmultiplatt.composeapp.generated.resources.Res
import knittingappmultiplatt.composeapp.generated.resources.common_back
import knittingappmultiplatt.composeapp.generated.resources.statistics_label_month
import knittingappmultiplatt.composeapp.generated.resources.statistics_label_year
import knittingappmultiplatt.composeapp.generated.resources.statistics_option_month
import knittingappmultiplatt.composeapp.generated.resources.statistics_option_total
import knittingappmultiplatt.composeapp.generated.resources.statistics_option_year
import knittingappmultiplatt.composeapp.generated.resources.statistics_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(onBack: () -> Unit) {
    var selectedOption by remember { mutableStateOf("Total") }
    var year by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.statistics_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(Res.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedOption == "Total",
                    onClick = { selectedOption = "Total" }
                )
                Text(stringResource(Res.string.statistics_option_total))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedOption == "Year",
                    onClick = { selectedOption = "Year" }
                )
                Text(stringResource(Res.string.statistics_option_year))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedOption == "Month",
                    onClick = { selectedOption = "Month" }
                )
                Text(stringResource(Res.string.statistics_option_month))
            }

            Spacer(Modifier.height(16.dp))

            if (selectedOption == "Year" || selectedOption == "Month") {
                TextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text(stringResource(Res.string.statistics_label_year)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (selectedOption == "Month") {
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text(stringResource(Res.string.statistics_label_month)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
