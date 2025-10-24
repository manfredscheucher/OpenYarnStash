package org.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import openyarnstash.composeapp.generated.resources.Res
import openyarnstash.composeapp.generated.resources.common_back
import openyarnstash.composeapp.generated.resources.common_plus_symbol
import openyarnstash.composeapp.generated.resources.project_list_empty
import openyarnstash.composeapp.generated.resources.project_list_title
import openyarnstash.composeapp.generated.resources.project_status_finished
import openyarnstash.composeapp.generated.resources.project_status_in_progress
import openyarnstash.composeapp.generated.resources.project_status_planning
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    projects: List<Project>,
    onAddClick: () -> Unit,
    onOpen: (Int) -> Unit,
    onBack: () -> Unit
) {
    AppBackHandler {
        onBack()
    }

    var activeStatuses by remember { mutableStateOf(setOf(ProjectStatus.IN_PROGRESS, ProjectStatus.PLANNING, ProjectStatus.FINISHED)) }

    val statusOrder = mapOf(
        ProjectStatus.IN_PROGRESS to 0,
        ProjectStatus.PLANNING to 1,
        ProjectStatus.FINISHED to 2
    )
    val filteredProjects = projects.filter { it.status in activeStatuses }
    val sortedProjects = filteredProjects.sortedWith(compareBy { statusOrder[it.status] })

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
        Column(Modifier.padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (status in ProjectStatus.values()) {
                    val statusText = when (status) {
                        ProjectStatus.PLANNING -> stringResource(Res.string.project_status_planning)
                        ProjectStatus.IN_PROGRESS -> stringResource(Res.string.project_status_in_progress)
                        ProjectStatus.FINISHED -> stringResource(Res.string.project_status_finished)
                    }
                    val count = projects.count { it.status == status }
                    val selected = status in activeStatuses
                    FilterChip(
                        selected = selected,
                        onClick = {
                            activeStatuses = if (selected) {
                                activeStatuses - status
                            } else {
                                activeStatuses + status
                            }
                        },
                        label = { Text("$statusText ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.outline,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                    )
                }
            }
            if (sortedProjects.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(16.dp)) {
                    Text(stringResource(Res.string.project_list_empty))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedProjects) { p ->
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
}
