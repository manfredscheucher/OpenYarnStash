package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    usagesForProject: List<Usage>,
    yarnNameById: (Int) -> String,
    onCancel: () -> Unit,
    onDelete: (Int) -> Unit,
    onSave: (Project) -> Unit,
    onNavigateToAssignments: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var url by remember { mutableStateOf(initial?.url ?: "") }
    var startDate by remember { mutableStateOf(initial?.startDate ?: "") }
    var endDate by remember { mutableStateOf(initial?.endDate ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var showDeleteRestrictionDialog by remember { mutableStateOf(false) }

    val status = when {
        endDate.isNotBlank() -> ProjectStatus.FINISHED
        startDate.isNotBlank() -> ProjectStatus.IN_PROGRESS
        else -> ProjectStatus.PLANNING
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initial == null) stringResource(Res.string.project_form_new) else stringResource(Res.string.project_form_edit)) },
                navigationIcon = { IconButton(onClick = onCancel) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.common_back)) } }
            )
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
            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text(stringResource(Res.string.project_label_start_date)) },
                supportingText = { Text(stringResource(Res.string.date_format_hint_project)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text(stringResource(Res.string.project_label_end_date)) },
                supportingText = { Text(stringResource(Res.string.date_format_hint_project)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            val statusText = when (status) {
                ProjectStatus.PLANNING -> stringResource(Res.string.project_status_planning)
                ProjectStatus.IN_PROGRESS -> stringResource(Res.string.project_status_in_progress)
                ProjectStatus.FINISHED -> stringResource(Res.string.project_status_finished)
            }
            Text("Status: $statusText", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(Res.string.project_label_notes)) },
                singleLine = false,
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            if (initial != null) {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(Res.string.usage_section_title), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (usagesForProject.isEmpty()) {
                    Text(stringResource(Res.string.project_form_no_yarn_assigned))
                } else {
                    usagesForProject.forEach { usage ->
                        Text("- ${yarnNameById(usage.yarnId)}: ${usage.amount} g")
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToAssignments,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.project_form_button_assignments))
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onCancel) { Text(stringResource(Res.string.common_cancel)) }
                Row {
                    if (initial != null) {
                        TextButton(onClick = {
                            if (usagesForProject.isNotEmpty()) {
                                showDeleteRestrictionDialog = true
                            } else {
                                onDelete(initial.id)
                            }
                        }) { Text(stringResource(Res.string.common_delete)) }
                        Spacer(Modifier.width(8.dp))
                    }
                    Button(onClick = {
                        val normalizedStartDate = normalizeDateString(startDate)
                        val normalizedEndDate = normalizeDateString(endDate)
                        val project = (initial ?: Project(id = -1, name = ""))
                            .copy(
                                name = name,
                                url = url.ifBlank { null },
                                startDate = normalizedStartDate,
                                endDate = normalizedEndDate,
                                notes = notes.ifBlank { null }
                            )
                        onSave(project)
                    }) { Text(stringResource(Res.string.common_save)) }
                }
            }
        }
        if (showDeleteRestrictionDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteRestrictionDialog = false },
                title = { Text(stringResource(Res.string.delete_project_restricted_title)) },
                text = { Text(stringResource(Res.string.delete_project_restricted_message)) },
                confirmButton = {
                    TextButton(onClick = { showDeleteRestrictionDialog = false }) { Text(stringResource(Res.string.common_ok)) }
                }
            )
        }
    }
}
