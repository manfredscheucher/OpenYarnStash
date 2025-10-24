package org.example.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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

    val statusOrder = mapOf(
        ProjectStatus.IN_PROGRESS to 0,
        ProjectStatus.PLANNING to 1,
        ProjectStatus.FINISHED to 2
    )
    val sortedProjects = projects.sortedWith(compareBy { statusOrder[it.status] })

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