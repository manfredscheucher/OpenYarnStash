package org.example.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import openyarnstash.composeapp.generated.resources.Res
import openyarnstash.composeapp.generated.resources.common_back
import openyarnstash.composeapp.generated.resources.common_cancel
import openyarnstash.composeapp.generated.resources.common_delete
import openyarnstash.composeapp.generated.resources.common_no
import openyarnstash.composeapp.generated.resources.common_ok
import openyarnstash.composeapp.generated.resources.common_save
import openyarnstash.composeapp.generated.resources.common_stay
import openyarnstash.composeapp.generated.resources.common_yes
import openyarnstash.composeapp.generated.resources.date_format_hint_project
import openyarnstash.composeapp.generated.resources.delete_project_restricted_message
import openyarnstash.composeapp.generated.resources.delete_project_restricted_title
import openyarnstash.composeapp.generated.resources.form_unsaved_changes_message
import openyarnstash.composeapp.generated.resources.form_unsaved_changes_title
import openyarnstash.composeapp.generated.resources.project_form_button_assignments
import openyarnstash.composeapp.generated.resources.project_form_edit
import openyarnstash.composeapp.generated.resources.project_form_new
import openyarnstash.composeapp.generated.resources.project_form_no_yarn_assigned
import openyarnstash.composeapp.generated.resources.project_label_end_date
import openyarnstash.composeapp.generated.resources.project_label_gauge
import openyarnstash.composeapp.generated.resources.project_label_name
import openyarnstash.composeapp.generated.resources.project_label_needle_size
import openyarnstash.composeapp.generated.resources.project_label_notes
import openyarnstash.composeapp.generated.resources.project_label_size
import openyarnstash.composeapp.generated.resources.project_label_start_date
import openyarnstash.composeapp.generated.resources.project_status_finished
import openyarnstash.composeapp.generated.resources.project_status_in_progress
import openyarnstash.composeapp.generated.resources.project_status_planning
import openyarnstash.composeapp.generated.resources.usage_section_title
import openyarnstash.composeapp.generated.resources.yarn_item_label_modified
import org.example.project.components.SelectAllOutlinedTextField
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectFormScreen(
    initial: Project,
    usagesForProject: List<Usage>,
    yarnNameById: (Int) -> String,
    onBack: () -> Unit,
    onDelete: (Int) -> Unit,
    onSave: (Project) -> Unit,
    onNavigateToAssignments: () -> Unit
) {
    val isNewProject = initial.id == -1

    var name by remember { mutableStateOf(initial.name) }
    var startDate by remember { mutableStateOf(initial.startDate ?: "") }
    var endDate by remember { mutableStateOf(initial.endDate ?: "") }
    var notes by remember { mutableStateOf(initial.notes ?: "") }
    var needleSize by remember { mutableStateOf(initial.needleSize ?: "") }
    var size by remember { mutableStateOf(initial.size ?: "") }
    var gauge by remember { mutableStateOf(initial.gauge ?: "") }
    val modified by remember { mutableStateOf(initial.modified) }
    var showDeleteRestrictionDialog by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val hasChanges by remember(initial, name, startDate, endDate, notes, needleSize, size, gauge) {
        derivedStateOf {
            name != initial.name ||
                    startDate != (initial.startDate ?: "") ||
                    endDate != (initial.endDate ?: "") ||
                    notes != (initial.notes ?: "") ||
                    needleSize != (initial.needleSize ?: "") ||
                    size != (initial.size ?: "") ||
                    gauge != (initial.gauge ?: "")
        }
    }

    val saveAction = {
        val normalizedStartDate = normalizeDateString(startDate)
        val normalizedEndDate = normalizeDateString(endDate)
        val project = initial.copy(
            name = name,
            startDate = normalizedStartDate,
            endDate = normalizedEndDate,
            notes = notes.ifBlank { null },
            modified = getCurrentTimestamp(),
            needleSize = needleSize.ifBlank { null },
            size = size.ifBlank { null },
            gauge = gauge.ifBlank { null }
        )
        onSave(project)
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
        UnsavedChangesDialog(
            onDismiss = { showUnsavedDialog = false },
            onStay = { showUnsavedDialog = false },
            onDiscard = {
                showUnsavedDialog = false
                onBack()
            },
            onSave = {
                saveAction()
                showUnsavedDialog = false
            }
        )
    }

    if (showDeleteRestrictionDialog) {
        DeleteRestrictionDialog(
            onDismiss = { showDeleteRestrictionDialog = false }
        )
    }

    val status = when {
        endDate.isNotBlank() -> ProjectStatus.FINISHED
        startDate.isNotBlank() -> ProjectStatus.IN_PROGRESS
        else -> ProjectStatus.PLANNING
    }

    Scaffold(
        topBar = {
            val titleRes = if (isNewProject) Res.string.project_form_new else Res.string.project_form_edit
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = { IconButton(onClick = backAction) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.common_back)) } }
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
            SelectAllOutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(Res.string.project_label_name)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text(stringResource(Res.string.project_label_start_date)) },
                supportingText = { Text(stringResource(Res.string.date_format_hint_project)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text(stringResource(Res.string.project_label_end_date)) },
                supportingText = { Text(stringResource(Res.string.date_format_hint_project)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(value = needleSize, onValueChange = { needleSize = it }, label = { Text(stringResource(Res.string.project_label_needle_size)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(value = size, onValueChange = { size = it }, label = { Text(stringResource(Res.string.project_label_size)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(value = gauge, onValueChange = { gauge = it }, label = { Text(stringResource(Res.string.project_label_gauge)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            val statusText = when (status) {
                ProjectStatus.PLANNING -> stringResource(Res.string.project_status_planning)
                ProjectStatus.IN_PROGRESS -> stringResource(Res.string.project_status_in_progress)
                ProjectStatus.FINISHED -> stringResource(Res.string.project_status_finished)
            }
            Text("Status: $statusText", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(Res.string.yarn_item_label_modified, modified))
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(Res.string.project_label_notes)) },
                singleLine = false,
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            if (!isNewProject) {
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
                TextButton(onClick = backAction) { Text(stringResource(Res.string.common_cancel)) }
                Row {
                    if (!isNewProject) {
                        TextButton(onClick = {
                            if (usagesForProject.isNotEmpty()) {
                                showDeleteRestrictionDialog = true
                            } else {
                                onDelete(initial.id)
                            }
                        }) { Text(stringResource(Res.string.common_delete)) }
                        Spacer(Modifier.width(8.dp))
                    }
                    Button(onClick = saveAction) { Text(stringResource(Res.string.common_save)) }
                }
            }
        }
    }
}

@Composable
private fun UnsavedChangesDialog(
    onDismiss: () -> Unit,
    onStay: () -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.form_unsaved_changes_title)) },
        text = { Text(stringResource(Res.string.form_unsaved_changes_message)) },
        confirmButton = {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onStay) {
                    Text(stringResource(Res.string.common_stay))
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDiscard) {
                    Text(stringResource(Res.string.common_no))
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onSave) {
                    Text(stringResource(Res.string.common_yes))
                }
            }
        },
        dismissButton = null
    )
}

@Composable
private fun DeleteRestrictionDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.delete_project_restricted_title)) },
        text = { Text(stringResource(Res.string.delete_project_restricted_message)) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_ok)) }
        }
    )
}
