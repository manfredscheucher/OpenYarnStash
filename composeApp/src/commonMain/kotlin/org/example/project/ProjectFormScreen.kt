package org.example.project

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import knittingappmultiplatt.composeapp.generated.resources.Res
import knittingappmultiplatt.composeapp.generated.resources.common_button_cancel
import knittingappmultiplatt.composeapp.generated.resources.common_button_delete
import knittingappmultiplatt.composeapp.generated.resources.common_button_save
import knittingappmultiplatt.composeapp.generated.resources.form_label_date_optional
import knittingappmultiplatt.composeapp.generated.resources.form_label_url_optional
import knittingappmultiplatt.composeapp.generated.resources.project_form_label_name
import knittingappmultiplatt.composeapp.generated.resources.project_form_title_edit
import knittingappmultiplatt.composeapp.generated.resources.project_form_title_new

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectFormScreen(
    initial: Project?,
    onCancel: () -> Unit,
    onDelete: (Int) -> Unit,
    onSave: (Project) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var url by remember { mutableStateOf(initial?.url ?: "") }
    var date by remember { mutableStateOf(initial?.date ?: "") }

    val title = if (initial == null) stringResource(Res.string.project_form_title_new) else stringResource(Res.string.project_form_title_edit)

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) },
        bottomBar = {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onCancel) { Text(stringResource(Res.string.common_button_cancel)) }
                Row {
                    if (initial != null) {
                        TextButton(onClick = { onDelete(initial.id) }) { Text(stringResource(Res.string.common_button_delete)) }
                        Spacer(Modifier.width(8.dp))
                    }
                    Button(onClick = {
                        val project = (initial ?: Project(id = -1, name = ""))
                            .copy(name = name, url = url.ifBlank { null }, date = date.ifBlank { null })
                        onSave(project)
                    }) { Text(stringResource(Res.string.common_button_save)) }
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(Res.string.project_form_label_name)) })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text(stringResource(Res.string.form_label_url_optional)) })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text(stringResource(Res.string.form_label_date_optional)) })
        }
    }
}
