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
fun ProjectListScreen(
    projects: List<Project>,
    onAddClick: () -> Unit,
    onOpen: (Int) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.project_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back))
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
        if (projects.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                Text(stringResource(Res.string.project_list_empty))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(projects) { p ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOpen(p.id) },
                        colors = CardDefaults.cardColors(containerColor = ColorPalette.idToColor(p.id))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(p.name, fontWeight = FontWeight.Bold)
                            val statusText = when (p.status) {
                                ProjectStatus.PLANNING -> stringResource(Res.string.project_status_planning)
                                ProjectStatus.IN_PROGRESS -> stringResource(Res.string.project_status_in_progress)
                                ProjectStatus.FINISHED -> stringResource(Res.string.project_status_finished)
                            }
                            Text(statusText)
                        }
                    }
                }
            }
        }
    }
}
