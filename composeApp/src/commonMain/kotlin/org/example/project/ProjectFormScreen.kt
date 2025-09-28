package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import knittingappmultiplatt.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectFormScreen(
    initial: Project?,
    onCancel: () -> Unit,
    onDelete: (Int) -> Unit,
    onSave: (Project) -> Unit, // Changed signature: no assignments map
    onNavigateToAssignments: () -> Unit // New navigation callback
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var url by remember { mutableStateOf(initial?.url ?: "") }
    var date by remember { mutableStateOf(initial?.date ?: "") }

    // Removed: var assignments by remember { mutableStateOf(currentAssignments.toMutableMap()) }

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
                        val project = (initial ?: Project(id = -1, name = "")) // ID will be set in App.kt if new
                            .copy(
                                name = name,
                                url = url.ifBlank { null },
                                date = date.ifBlank { null }
                            )
                        // Removed: val clean = assignments.filterValues { it > 0 }
                        onSave(project) // Changed call: only project details
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

            // --- Removed yarn assignment section --- 
            // Spacer(Modifier.height(16.dp))
            // Text(stringResource(Res.string.usage_section_title), style = MaterialTheme.typography.titleMedium)
            // Spacer(Modifier.height(8.dp))
            // Column(modifier = Modifier.fillMaxWidth()) { ... old assignment UI ... }

            if (initial != null) {
                Spacer(Modifier.height(24.dp)) // Add some space before the new button
                Button(
                    onClick = onNavigateToAssignments,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.project_form_button_assignments))
                }
            }
        }
    }
}
