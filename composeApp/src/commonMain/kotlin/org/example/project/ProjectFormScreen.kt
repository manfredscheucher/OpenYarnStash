package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
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
import openyarnstash.composeapp.generated.resources.delete_project_restricted_message
import openyarnstash.composeapp.generated.resources.delete_project_restricted_title
import openyarnstash.composeapp.generated.resources.form_unsaved_changes_message
import openyarnstash.composeapp.generated.resources.form_unsaved_changes_title
import openyarnstash.composeapp.generated.resources.project_form_add_counter
import openyarnstash.composeapp.generated.resources.project_form_add_counter_dialog_label
import openyarnstash.composeapp.generated.resources.project_form_add_counter_dialog_title
import openyarnstash.composeapp.generated.resources.project_form_button_assignments
import openyarnstash.composeapp.generated.resources.project_form_edit
import openyarnstash.composeapp.generated.resources.project_form_new
import openyarnstash.composeapp.generated.resources.project_form_no_yarn_assigned
import openyarnstash.composeapp.generated.resources.project_form_remove_image
import openyarnstash.composeapp.generated.resources.project_form_select_image
import openyarnstash.composeapp.generated.resources.project_label_end_date
import openyarnstash.composeapp.generated.resources.project_label_for
import openyarnstash.composeapp.generated.resources.project_label_gauge
import openyarnstash.composeapp.generated.resources.project_label_name
import openyarnstash.composeapp.generated.resources.project_label_needle_size
import openyarnstash.composeapp.generated.resources.project_label_notes
import openyarnstash.composeapp.generated.resources.project_label_row_count
import openyarnstash.composeapp.generated.resources.project_label_size
import openyarnstash.composeapp.generated.resources.project_label_start_date
import openyarnstash.composeapp.generated.resources.project_status_finished
import openyarnstash.composeapp.generated.resources.project_status_in_progress
import openyarnstash.composeapp.generated.resources.project_status_planning
import openyarnstash.composeapp.generated.resources.projects
import openyarnstash.composeapp.generated.resources.usage_section_title
import openyarnstash.composeapp.generated.resources.yarn_item_label_modified
import org.example.project.components.DateInput
import org.example.project.components.SelectAllOutlinedTextField
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectFormScreen(
    initial: Project,
    initialImage: ByteArray?,
    usagesForProject: List<Usage>,
    yarnById: (Int) -> Yarn?,
    onBack: () -> Unit,
    onDelete: (Int) -> Unit,
    onSave: (Project, ByteArray?) -> Unit,
    onNavigateToAssignments: () -> Unit
) {
    val isNewProject = initial.id == -1

    var name by remember { mutableStateOf(initial.name) }
    var forWho by remember { mutableStateOf(initial.madeFor ?: "") }
    var startDate by remember { mutableStateOf(initial.startDate?: "") }
    var endDate by remember { mutableStateOf(initial.endDate?: "") }
    var notes by remember { mutableStateOf(initial.notes ?: "") }
    var needleSize by remember { mutableStateOf(initial.needleSize ?: "") }
    var size by remember { mutableStateOf(initial.size ?: "") }
    var gauge by remember { mutableStateOf(initial.gauge?.toString() ?: "") }
    var rowCounters by remember { mutableStateOf(initial.rowCounters) }
    val modified by remember { mutableStateOf(initial.modified) }
    var showDeleteRestrictionDialog by remember { mutableStateOf(false) }
    var showUnsavedDialogForBack by remember { mutableStateOf(false) }
    var showUnsavedDialogForAssignments by remember { mutableStateOf(false) }
    var newImage by remember { mutableStateOf<ByteArray?>(null) }
    var showAddCounterDialog by remember { mutableStateOf(false) }

    val imagePicker = rememberImagePickerLauncher {
        newImage = it
    }

    val hasChanges by remember(initial, name, forWho, startDate, endDate, notes, needleSize, size, gauge, newImage, rowCounters) {
        derivedStateOf {
            name != initial.name ||
                    forWho != (initial.madeFor ?: "") ||
                    startDate != initial.startDate ||
                    endDate != initial.endDate ||
                    notes != (initial.notes ?: "") ||
                    needleSize != (initial.needleSize ?: "") ||
                    size != (initial.size ?: "") ||
                    gauge != (initial.gauge?.toString() ?: "") ||
                    newImage != null ||
                    rowCounters != initial.rowCounters
        }
    }

    val saveAction = {
        val project = initial.copy(
            name = name,
            madeFor = forWho.ifBlank { null },
            startDate = startDate,
            endDate = endDate,
            notes = notes.ifBlank { null },
            modified = getCurrentTimestamp(),
            needleSize = needleSize.ifBlank { null },
            size = size.ifBlank { null },
            gauge = gauge.toIntOrNull(),
            rowCounters = rowCounters
        )
        onSave(project, newImage)
    }

    val backAction = {
        if (hasChanges) {
            showUnsavedDialogForBack = true
        } else {
            onBack()
        }
    }

    val assignAction = {
        if (hasChanges) {
            showUnsavedDialogForAssignments = true
        } else {
            onNavigateToAssignments()
        }
    }

    AppBackHandler {
        backAction()
    }

    if (showUnsavedDialogForBack) {
        UnsavedChangesDialog(
            onDismiss = { showUnsavedDialogForBack = false },
            onStay = { showUnsavedDialogForBack = false },
            onDiscard = {
                showUnsavedDialogForBack = false
                onBack()
            },
            onSave = {
                saveAction()
                showUnsavedDialogForBack = false
                onBack()
            }
        )
    }

    if (showUnsavedDialogForAssignments) {
        UnsavedChangesDialog(
            onDismiss = { showUnsavedDialogForAssignments = false },
            onStay = { showUnsavedDialogForAssignments = false },
            onDiscard = {
                showUnsavedDialogForAssignments = false
                onNavigateToAssignments()
            },
            onSave = {
                saveAction()
                showUnsavedDialogForAssignments = false
                onNavigateToAssignments()
            }
        )
    }

    if (showDeleteRestrictionDialog) {
        DeleteRestrictionDialog(
            onDismiss = { showDeleteRestrictionDialog = false }
        )
    }

    if (showAddCounterDialog) {
        AddCounterDialog(
            onDismiss = { showAddCounterDialog = false },
            onAdd = {
                rowCounters = rowCounters + RowCounter(it, 0)
                showAddCounterDialog = false
            }
        )
    }

    val status = when {
        !endDate.isNullOrBlank() -> ProjectStatus.FINISHED
        !startDate.isNullOrBlank() -> ProjectStatus.IN_PROGRESS
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
            val displayedImage = newImage ?: initialImage
            if (displayedImage != null) {
                val bitmap: ImageBitmap? = remember(displayedImage) { displayedImage.toImageBitmap() }
                if (bitmap != null) {
                    Image(bitmap, contentDescription = "Project Image", modifier = Modifier.fillMaxWidth().height(200.dp))
                }
            } else {
                Image(
                    painter = painterResource(Res.drawable.projects),
                    contentDescription = "Project icon",
                    modifier = Modifier.fillMaxWidth().height(200.dp).alpha(0.5f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { imagePicker.launch() }) {
                    Text(stringResource(Res.string.project_form_select_image))
                }
                if (displayedImage != null) {
                    Button(onClick = { newImage = createEmptyImageByteArray() }) {
                        Text(stringResource(Res.string.project_form_remove_image))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            SelectAllOutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(Res.string.project_label_name)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(value = forWho, onValueChange = { forWho = it }, label = { Text(stringResource(Res.string.project_label_for)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            DateInput(
                label = stringResource(Res.string.project_label_start_date),
                date = startDate,
                onDateChange = { startDate = it?: "" }
            )
            Spacer(Modifier.height(8.dp))
            DateInput(
                label = stringResource(Res.string.project_label_end_date),
                date = endDate,
                onDateChange = { endDate = it?: "" }
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectAllOutlinedTextField(value = needleSize, onValueChange = { needleSize = it }, label = { Text(stringResource(Res.string.project_label_needle_size)) }, modifier = Modifier.weight(1f))
                SelectAllOutlinedTextField(value = size, onValueChange = { size = it }, label = { Text(stringResource(Res.string.project_label_size)) }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(
                value = gauge,
                onValueChange = { gauge = it },
                label = { Text(stringResource(Res.string.project_label_gauge)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            val rowHeight = 50
            Box(modifier = Modifier.fillMaxWidth()) {
                val totalHeight = if (rowCounters.isNotEmpty()) {
                    (rowCounters.size  * rowHeight).dp + 60.dp
                } else {
                    64.dp
                }
                OutlinedTextField(
                    value = " ",
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(totalHeight),
                    readOnly = true,
                    label = { Text(stringResource(Res.string.project_label_row_count)) }
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp)
                ) {
                    rowCounters.forEachIndexed { index, counter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth().padding(end=20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(counter.name, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.weight(0.6f))
                            IconButton(onClick = {
                                rowCounters = rowCounters.toMutableList().also {
                                    it[index] = it[index].copy(value = it[index].value - 1)
                                }
                            }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease row count")
                            }
                            Text(text = counter.value.toString(), style = MaterialTheme.typography.bodyLarge)
                            IconButton(onClick = {
                                rowCounters = rowCounters.toMutableList().also {
                                    it[index] = it[index].copy(value = it[index].value + 1)
                                }
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase row count")
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        horizontalArrangement = Arrangement.End){
                        Button(onClick = { showAddCounterDialog = true }) {
                            Text(stringResource(Res.string.project_form_add_counter))
                    }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            val statusText = when (status) {
                ProjectStatus.PLANNING -> stringResource(Res.string.project_status_planning)
                ProjectStatus.IN_PROGRESS -> stringResource(Res.string.project_status_in_progress)
                ProjectStatus.FINISHED -> stringResource(Res.string.project_status_finished)
            }
            Text("Status: $statusText", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            if (modified != null) {
                Text(stringResource(Res.string.yarn_item_label_modified, modified?: ""))
                Spacer(Modifier.height(8.dp))
            }
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
                        val yarn = yarnById(usage.yarnId)
                        if (yarn != null) {
                            Text(buildAnnotatedString {
                                yarn.brand?.takeIf { it.isNotBlank() }?.let {
                                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.SemiBold)) {
                                        append("$it ")
                                    }
                                }
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(yarn.name)
                                }
                                yarn.color?.takeIf { it.isNotBlank() }?.let {
                                    append(" ($it)")
                                }
                                append(": ${usage.amount} g")
                            })
                        } else {
                            Text("- ${usage.yarnId}: ${usage.amount} g")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = assignAction,
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
private fun AddCounterDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.project_form_add_counter_dialog_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.project_form_add_counter_dialog_label)) },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onAdd(name) }) {
                Text(stringResource(Res.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.common_cancel))
            }
        }
    )
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

expect fun ByteArray.toImageBitmap(): ImageBitmap