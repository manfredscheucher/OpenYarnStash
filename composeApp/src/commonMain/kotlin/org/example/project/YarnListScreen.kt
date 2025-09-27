package org.example.project

import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YarnListScreen(
    yarns: List<Yarn>,
    onAddClick: () -> Unit,
    onOpen: (Int) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Woll-Stash") }) },
        floatingActionButton = { FloatingActionButton(onClick = onAddClick) { Text("+") } }
    ) { padding ->
        if (yarns.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Noch keine Wolle. Tippe auf +")
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(contentPadding = padding) {
                items(yarns) { yarn ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(yarn.id) }
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("${yarn.name} (${yarn.color ?: "?"})")
                            Text("${yarn.amount} g")
                            yarn.url?.let { Text(it) }
                            yarn.date?.let { Text(it) }
                        }
                    }
                    Divider()
                }
            }
        }
    }
}

