package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource // Added import
import knittingappmultiplatt.composeapp.generated.resources.*

// Removed import for tr, and specific string resource like yarn_form_edit as Res should cover it.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YarnFormScreen(
    initial: Yarn?,
    usagesForYarn: List<Usage>,
    projectNameById: (Int) -> String,
    onCancel: () -> Unit,
    onDelete: (Int) -> Unit,
    onSave: (Yarn) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var color by remember { mutableStateOf(initial?.color ?: "") }
    var amount by remember { mutableStateOf((initial?.amount ?: 0).toString()) }
    var url by remember { mutableStateOf(initial?.url ?: "") }
    var date by remember { mutableStateOf(initial?.date ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (initial == null) stringResource(Res.string.yarn_form_new) else stringResource(Res.string.yarn_form_edit))
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
                        TextButton(onClick = { onDelete(initial.id) }) { Text(stringResource(Res.string.common_delete)) }
                        Spacer(Modifier.width(8.dp))
                    }
                    Button(onClick = {
                        val amt = amount.filter { it.isDigit() }.toIntOrNull() ?: 0
                        val yarn = (initial ?: Yarn(id = -1, name = "", amount = 0))
                            .copy(
                                name = name,
                                color = color.ifBlank { null },
                                amount = amt,
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
            OutlinedTextField(value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() } }, label = { Text(stringResource(Res.string.yarn_label_amount)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text(stringResource(Res.string.yarn_label_url)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text(stringResource(Res.string.yarn_label_date)) }, modifier = Modifier.fillMaxWidth())

            if (initial != null) {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(Res.string.usage_projects_title), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                val usedByProjects = usagesForYarn.sumOf { it.amount }
                val totalAmount = initial.amount
                val availableForNewAssignments = (totalAmount - usedByProjects).coerceAtLeast(0)

                Text(stringResource(Res.string.usage_used, usedByProjects))
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
