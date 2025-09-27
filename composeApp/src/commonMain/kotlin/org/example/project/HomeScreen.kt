package org.example.project

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenYarns: () -> Unit,
    onOpenProjects: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("OpenKnit") }) }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Button(onClick = onOpenYarns) { Text("Yarns") }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onOpenProjects) { Text("Projects") }
        }
    }
}
