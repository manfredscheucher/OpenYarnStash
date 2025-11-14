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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import openyarnstash.composeapp.generated.resources.Res
import openyarnstash.composeapp.generated.resources.common_back
import openyarnstash.composeapp.generated.resources.common_cancel
import openyarnstash.composeapp.generated.resources.common_delete
import openyarnstash.composeapp.generated.resources.common_no
import openyarnstash.composeapp.generated.resources.common_save
import openyarnstash.composeapp.generated.resources.common_stay
import openyarnstash.composeapp.generated.resources.common_yes
import openyarnstash.composeapp.generated.resources.form_unsaved_changes_message
import openyarnstash.composeapp.generated.resources.form_unsaved_changes_title
import openyarnstash.composeapp.generated.resources.pattern_form_edit
import openyarnstash.composeapp.generated.resources.pattern_form_new
import openyarnstash.composeapp.generated.resources.pattern_label_creator
import openyarnstash.composeapp.generated.resources.pattern_label_name
import openyarnstash.composeapp.generated.resources.pattern_label_gauge
import openyarnstash.composeapp.generated.resources.pattern_form_select_pdf
import openyarnstash.composeapp.generated.resources.pattern_form_remove_pdf
import openyarnstash.composeapp.generated.resources.pattern_form_view_pdf
import openyarnstash.composeapp.generated.resources.pattern_form_no_projects_assigned
import openyarnstash.composeapp.generated.resources.pattern_form_assigned_projects
import openyarnstash.composeapp.generated.resources.pattern_form_view_project
import openyarnstash.composeapp.generated.resources.pattern_label_category
import openyarnstash.composeapp.generated.resources.projects
import org.example.project.components.SelectAllOutlinedTextField
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternFormScreen(
    initial: Pattern?,
    initialPdf: ByteArray?,
    projects: List<Project>,
    patterns: List<Pattern>,
    imageManager: ImageManager,
    onBack: () -> Unit,
    onDelete: (Int) -> Unit,
    onSave: (Pattern, ByteArray?) -> Unit,
    onViewPdf: () -> Unit,
    onNavigateToProject: (Int) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var creator by remember { mutableStateOf(initial?.creator ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "") }
    var gauge by remember { mutableStateOf(initial?.gauge ?: "") }
    var pdf by remember { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(initialPdf) {
        pdf = initialPdf
    }

    var showUnsavedDialog by remember { mutableStateOf(false) }
    var onConfirmUnsaved by remember { mutableStateOf<() -> Unit>({}) }

    val pdfPicker = rememberPdfPickerLauncher {
        pdf = it
    }

    val hasChanges by remember(name, creator, category, gauge, pdf) {
        derivedStateOf {
            if (initial == null) {
                name.isNotEmpty() || creator.isNotEmpty() || category.isNotEmpty() || gauge.isNotEmpty() || pdf != null
            } else {
                name != initial.name || creator != (initial.creator ?: "") || category != (initial.category ?: "") || gauge != (initial.gauge ?: "") || pdf != initialPdf
            }
        }
    }

    val saveAction = {
        val pattern = (initial ?: Pattern(id = -1, name = "")).copy(
            name = name,
            creator = creator.ifBlank { null },
            category = category.ifBlank { null },
            gauge = gauge.ifBlank { null }
        )
        onSave(pattern, pdf)
    }

    val confirmDiscardChanges = { onConfirm: () -> Unit ->
        if (hasChanges) {
            showUnsavedDialog = true
            onConfirmUnsaved = onConfirm
        } else {
            onConfirm()
        }
    }

    val backAction = {
        confirmDiscardChanges(onBack)
    }

    BackButtonHandler {
        backAction()
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text(stringResource(Res.string.form_unsaved_changes_title)) },
            text = { Text(stringResource(Res.string.form_unsaved_changes_message)) },
            confirmButton = {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showUnsavedDialog = false }) {
                        Text(stringResource(Res.string.common_stay))
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        showUnsavedDialog = false
                        onConfirmUnsaved()
                    }) {
                        Text(stringResource(Res.string.common_no))
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        saveAction()
                        showUnsavedDialog = false
                        onConfirmUnsaved()
                    }) {
                        Text(stringResource(Res.string.common_yes))
                    }
                }
            },
            dismissButton = null
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initial == null) stringResource(Res.string.pattern_form_new) else stringResource(Res.string.pattern_form_edit)) },
                navigationIcon = {
                    IconButton(onClick = backAction) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back))
                    }
                }
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                SelectAllOutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(Res.string.pattern_label_name)) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                SelectAllOutlinedTextField(value = creator, onValueChange = { creator = it }, label = { Text(stringResource(Res.string.pattern_label_creator)) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                var expanded by remember { mutableStateOf(false) }
                val categories = patterns.mapNotNull { it.category }.filter { it.isNotBlank() }.distinct()

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        value = category,
                        onValueChange = { category = it },
                        label = { Text(stringResource(Res.string.pattern_label_category)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        categories.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    category = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                SelectAllOutlinedTextField(value = gauge, onValueChange = { gauge = it }, label = { Text(stringResource(Res.string.pattern_label_gauge)) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                if (pdf == null) {
                    Button(onClick = { pdfPicker("application/pdf") }) {
                        Text(stringResource(Res.string.pattern_form_select_pdf))
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Button(onClick = { confirmDiscardChanges(onViewPdf) }) {
                            Text(stringResource(Res.string.pattern_form_view_pdf))
                        }
                        Button(onClick = { pdf = null }) {
                            Text(stringResource(Res.string.pattern_form_remove_pdf))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                val projectsWithPattern = projects.filter { it.patternId == initial?.id }
                if (projectsWithPattern.isNotEmpty()) {
                    Text(stringResource(Res.string.pattern_form_assigned_projects))
                    Spacer(Modifier.height(8.dp))
                    projectsWithPattern.forEach { project ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
                            LaunchedEffect(project.id, project.imageIds) {
                                val imageId = project.imageIds.firstOrNull()
                                imageBytes = if (imageId != null) {
                                    imageManager.getProjectImageThumbnail(project.id, imageId)
                                } else {
                                    null
                                }
                            }
                            val bitmap = remember(imageBytes) { imageBytes?.toImageBitmap() }

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "Project image",
                                    modifier = Modifier.size(40.dp)
                                )
                            } else {
                                Image(
                                    painter = painterResource(Res.drawable.projects),
                                    contentDescription = "Default project image",
                                    modifier = Modifier.size(40.dp).alpha(0.5f)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(project.name, modifier = Modifier.weight(1f))
                            Button(onClick = { confirmDiscardChanges { onNavigateToProject(project.id) } }) {
                                Text(stringResource(Res.string.pattern_form_view_project))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                } else {
                    Text(stringResource(Res.string.pattern_form_no_projects_assigned))
                }
                Spacer(Modifier.height(24.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = backAction) { Text(stringResource(Res.string.common_cancel)) }
                    Row {
                        if (initial != null) {
                            TextButton(onClick = { onDelete(initial.id) }) { Text(stringResource(Res.string.common_delete)) }
                            Spacer(Modifier.width(8.dp))
                        }
                        Button(onClick = saveAction) { Text(stringResource(Res.string.common_save)) }
                    }
                }
            }
        }
    }
}
