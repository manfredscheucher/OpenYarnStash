package org.example.project

import androidx.compose.foundation.background
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
import org.jetbrains.compose.resources.stringResource
import openyarnstash.composeapp.generated.resources.*

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
        floatingActionButton = { FloatingActionButton(onClick = onAddClick) { Text(stringResource(Res.string.common_plus_symbol)) } },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        if (projects.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                Text(stringResource(Res.string.project_list_empty))
            }
        } else {
            LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
                items(projects) { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = ColorPalette.idToColor(p.id))
                            .clickable { onOpen(p.id) }
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(stringResource(Res.string.item_label_name, p.name))
                            p.url?.let { Text(stringResource(Res.string.item_label_url, it)) }
                            val statusText = when (p.status) {
                                ProjectStatus.PLANNING -> stringResource(Res.string.project_status_planning)
                                ProjectStatus.IN_PROGRESS -> stringResource(Res.string.project_status_in_progress)
                                ProjectStatus.FINISHED -> stringResource(Res.string.project_status_finished)
                            }
                            Text(statusText)
                        }
                    }
                    Divider()
                }
            }
        }
    }
}
