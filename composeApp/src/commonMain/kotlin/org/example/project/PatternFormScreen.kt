package org.example.project

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import org.example.project.components.SelectAllOutlinedTextField
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternFormScreen(
    initial: Pattern?,
    onBack: () -> Unit,
    onDelete: (Int) -> Unit,
    onSave: (Pattern) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var creator by remember { mutableStateOf(initial?.creator ?: "") }
    var gauge by remember { mutableStateOf(initial?.gauge ?: "") }

    var showUnsavedDialog by remember { mutableStateOf(false) }

    val hasChanges by remember(name, creator, gauge) {
        derivedStateOf {
            if (initial == null) {
                name.isNotEmpty() || creator.isNotEmpty() || gauge.isNotEmpty()
            } else {
                name != initial.name || creator != (initial.creator ?: "") || gauge != (initial.gauge ?: "")
            }
        }
    }

    val saveAction = {
        val pattern = (initial ?: Pattern(id = -1, name = "")).copy(
            name = name,
            creator = creator.ifBlank { null },
            gauge = gauge.ifBlank { null }
        )
        onSave(pattern)
    }

    val backAction = {
        if (hasChanges) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
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
                        onBack()
                    }) {
                        Text(stringResource(Res.string.common_no))
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        saveAction()
                        showUnsavedDialog = false
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
                SelectAllOutlinedTextField(value = gauge, onValueChange = { gauge = it }, label = { Text(stringResource(Res.string.pattern_label_gauge)) }, modifier = Modifier.fillMaxWidth())
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
