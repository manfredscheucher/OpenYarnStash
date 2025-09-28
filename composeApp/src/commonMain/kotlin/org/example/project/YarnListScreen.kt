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
fun YarnListScreen(
    yarns: List<Yarn>,
    onAddClick: () -> Unit,
    onOpen: (Int) -> Unit,
    onBack: () -> Unit      // <-- new
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yarn Stash") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) { Text("+") }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        if (yarns.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("No yarn yet. Tap on +")
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
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
