package org.example.project

import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YarnFormScreen(
    initial: Yarn?,                  // null = neu
    onCancel: () -> Unit,
    onDelete: (Int) -> Unit,         // gibt id
    onSave: (Yarn) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var color by remember { mutableStateOf(initial?.color ?: "") }
    var amountText by remember { mutableStateOf((initial?.amount ?: 0).toString()) }
    var url by remember { mutableStateOf(initial?.url ?: "") }
    var date by remember { mutableStateOf(initial?.date ?: "") }

    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (initial == null) "Neue Wolle" else "Wolle bearbeiten") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name*") }, singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Farbe") }, singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Menge (g)*") }, singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL (optional)") }, singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Datum (optional, z.B. 2025-09-26)") }, singleLine = true)

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    // Validierung
                    val amt = amountText.toIntOrNull()
                    if (name.isBlank()) { error = "Name darf nicht leer sein"; return@Button }
                    if (amt == null || amt < 0) { error = "Menge muss eine Zahl ≥ 0 sein"; return@Button }

                    val y = Yarn(
                        id = initial?.id ?: -1, // -1 = wird in App vergeben
                        name = name.trim(),
                        color = color.ifBlank { null },
                        amount = amt,
                        url = url.ifBlank { null },
                        date = date.ifBlank { null }
                    )
                    onSave(y)
                }) {
                    Text("Speichern")
                }
                OutlinedButton(onClick = onCancel) { Text("Abbrechen") }
                if (initial != null) {
                    OutlinedButton(
                        onClick = { onDelete(initial.id) },
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) { Text("Löschen") }
                }
            }
        }
    }
}
