package org.example.project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import knittingappmultiplatt.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YarnListScreen(
    yarns: List<Yarn>,
    usages: List<Usage>,
    onAddClick: () -> Unit,
    onOpen: (Int) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.yarn_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = onAddClick) { Text(stringResource(Res.string.common_plus_symbol)) } },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (yarns.isEmpty()) {
                 Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp) 
                ) {
                    Text(stringResource(Res.string.yarn_list_empty))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 96.dp) 
                ) {
                    items(yarns) { yarn ->
                        val used = usages.filter { it.yarnId == yarn.id }.sumOf { it.amount }
                        val available = (yarn.amount - used).coerceAtLeast(0)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpen(yarn.id) }
                                .padding(16.dp)
                        ) {
                            Column {
                                Text("${yarn.name} (${yarn.color ?: "?"})")
                                yarn.brand?.let { Text(stringResource(Res.string.yarn_label_brand) + ": " + it) } // Display brand if available
                                Text("${yarn.amount} g")
                                Text(stringResource(Res.string.usage_used, used))
                                Text(stringResource(Res.string.usage_available, available))
                                yarn.url?.let { Text(stringResource(Res.string.item_label_url, it)) }
                                yarn.dateAdded?.let { Text(stringResource(Res.string.yarn_item_label_date_added, it)) } // Changed to dateAdded and new string resource
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }
}
