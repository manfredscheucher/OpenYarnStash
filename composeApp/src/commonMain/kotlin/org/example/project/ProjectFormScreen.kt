package org.example.project

import androidx.compose.foundation.layout.*
// import androidx.compose.foundation.lazy.LazyColumn // Not used after previous change
// import androidx.compose.foundation.lazy.items // Not used after previous change
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource // Added import
import knittingappmultiplatt.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectFormScreen(
    initial: Project?,
    yarns: List<Yarn>,
    currentAssignments: Map<Int, Int>,
    availableForYarn: (Int) -> Int,
    onCancel: () -> Unit,
    onDelete: (Int) -> Unit,
    onSave: (Project, Map<Int, Int>) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var url by remember { mutableStateOf(initial?.url ?: "") }
    var date by remember { mutableStateOf(initial?.date ?: "") }

    var assignments by remember { mutableStateOf(currentAssignments.toMutableMap()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (initial == null) stringResource(Res.string.project_form_new) else stringResource(Res.string.project_form_edit))
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
                        val project = (initial ?: Project(id = -1, name = ""))
                            .copy(
                                name = name,
                                url = url.ifBlank { null },
                                date = date.ifBlank { null }
                            )
                        val clean = assignments.filterValues { it > 0 }
                        onSave(project, clean)
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
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(Res.string.project_label_name)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text(stringResource(Res.string.project_label_url)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text(stringResource(Res.string.project_label_date)) }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))
            Text(stringResource(Res.string.usage_section_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                yarns.forEach { y ->
                    val avail = availableForYarn(y.id)
                    val current = assignments[y.id] ?: 0

                    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("${y.name} (${y.color ?: "?"}) — ${y.amount} g")
                        Text(stringResource(Res.string.usage_available, avail))
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = current.toString(),
                            onValueChange = { text ->
                                val raw = text.filter { it.isDigit() }
                                val num = raw.toIntOrNull() ?: 0
                                val clamped = num.coerceIn(0, avail + (currentAssignments[y.id] ?:0) )
                                assignments = assignments.toMutableMap().also { it[y.id] = clamped }
                            },
                            label = { Text(stringResource(Res.string.usage_amount_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Divider(Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}
