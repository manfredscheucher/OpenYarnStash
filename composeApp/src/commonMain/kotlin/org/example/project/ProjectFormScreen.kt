package org.example.project

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (initial == null) "New Project" else "Edit Project") }) },
        bottomBar = {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onCancel) { Text("Cancel") }
                Row {
                    if (initial != null) {
                        TextButton(onClick = { onDelete(initial.id) }) { Text("Delete") }
                        Spacer(Modifier.width(8.dp))
                    }
                    Button(onClick = {
                        val project = (initial ?: Project(id = -1, name = ""))
                            .copy(name = name, url = url.ifBlank { null }, date = date.ifBlank { null })
                        onSave(project)
                    }) { Text("Save") }
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL (optional)") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (optional)") })
        }
    }
}
