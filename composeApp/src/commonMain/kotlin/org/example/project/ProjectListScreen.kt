package org.example.project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    projects: List<Project>,
    onAddClick: () -> Unit,
    onOpen: (Int) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projects") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = onAddClick) { Text("+") } },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        if (projects.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                Text("No projects yet. Tap +")
            }
        } else {
            LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
                items(projects) { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(p.id) }
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(p.name)
                            p.url?.let { Text(it) }
                            p.date?.let { Text(it) }
                        }
                    }
                    Divider()
                }
            }
        }
    }
}
