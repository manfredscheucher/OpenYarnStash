package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import openyarnstash.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YarnListScreen(
    yarns: List<Yarn>,
    usages: List<Usage>,
    onAddClick: () -> Unit,
    onOpen: (Int) -> Unit,
    onBack: () -> Unit
) {
    AppBackHandler {
        onBack()
    }

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
        floatingActionButton = {
            LargeFloatingActionButton(onClick = onAddClick) {
                Text(
                    text = stringResource(Res.string.common_plus_symbol),
                    style = MaterialTheme.typography.displayMedium
                )
            }
        },
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
                val totalAvailable = yarns.sumOf { yarn ->
                    val used = usages.filter { it.yarnId == yarn.id }.sumOf { it.amount }
                    (yarn.amount - used).coerceAtLeast(0)
                }
                Text(
                    text = stringResource(Res.string.yarn_list_summary, totalAvailable),
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(yarns) { yarn ->
                        val used = usages.filter { it.yarnId == yarn.id }.sumOf { it.amount }
                        val available = (yarn.amount - used).coerceAtLeast(0)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            onClick = { onOpen(yarn.id) },
                            colors = CardDefaults.cardColors(containerColor = ColorPalette.idToColor(yarn.id))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val brandText = yarn.brand?.let { "$it " } ?: ""
                                Text("$brandText${yarn.name} (${yarn.color ?: "?"})", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stringResource(Res.string.usage_used, used))
                                    Text(stringResource(Res.string.usage_available, available))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
