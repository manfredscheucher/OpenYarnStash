package org.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import openyarnstash.composeapp.generated.resources.Res
import openyarnstash.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    projects: List<Project>,
    projectImages: Map<Int, ByteArray?>,
    yarns: List<Yarn>,
    usages: List<Usage>,
    yarnImages: Map<Int, ByteArray?>,
    settings: Settings,
    onAddClick: () -> Unit,
    onOpen: (Int) -> Unit,
    onBack: () -> Unit,
    onSettingsChange: (Settings) -> Unit
) {
    AppBackHandler {
        onBack()
    }

    var activeStatuses by remember {
        mutableStateOf(
            ProjectStatus.values().filter {
                settings.projectToggles[it.name] ?: true
            }.toSet()
        )
    }
    var filter by remember { mutableStateOf("") }

    val filteredProjects = projects.filter {
        val statusOk = it.status in activeStatuses
        val filterOk = if (filter.isNotBlank()) {
            it.name.contains(filter, ignoreCase = true)
        } else {
            true
        }
        statusOk && filterOk
    }
    val sortedProjects = filteredProjects.sortedByDescending { it.modified }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(Res.drawable.projects),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.project_list_title))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back))
                    }
                },
                modifier = Modifier.padding(top = 2.dp)
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
                            val newActiveStatuses = if (selected) {
                                activeStatuses - status
                            } else {
                                activeStatuses + status
                            }
                            activeStatuses = newActiveStatuses

                            val newProjectToggles = ProjectStatus.values().associate {
                                it.name to (it in newActiveStatuses)
                            }
                            onSettingsChange(settings.copy(projectToggles = newProjectToggles))
                        },
                        label = { Text("$statusText ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    )
                }
            }
            OutlinedTextField(
                value = filter,
                onValueChange = { filter = it },
                label = { Text(stringResource(Res.string.project_list_filter)) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = stringResource(Res.string.project_list_filter)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            if (sortedProjects.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(16.dp)) {
                    Text(stringResource(Res.string.project_list_empty))
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    val state = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        state = state,
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {                        items(sortedProjects) { p ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onOpen(p.id) },
                                colors = CardDefaults.cardColors(containerColor = ColorPalette.idToColor(p.id))
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    val imageBytes = projectImages[p.id]
                                    if (imageBytes != null) {
                                        val bitmap = remember(imageBytes) { imageBytes.toImageBitmap() }
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = "Project image for ${'$'}{p.name}",
                                            modifier = Modifier.size(64.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(Res.drawable.projects),
                                            contentDescription = "Project icon",
                                            modifier = Modifier.size(64.dp).alpha(0.5f),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(p.name, fontWeight = FontWeight.Bold)
                                        val statusText = when (p.status) {
                                            ProjectStatus.PLANNING -> stringResource(Res.string.project_status_planning)
                                            ProjectStatus.IN_PROGRESS -> stringResource(Res.string.project_status_in_progress)
                                            ProjectStatus.FINISHED -> stringResource(Res.string.project_status_finished)
                                        }
                                        Text(statusText)





                                        Spacer(Modifier.height(8.dp))

                                        val yarnsForProject = remember(p.id, usages, yarns) {
                                            usages.filter { it.projectId == p.id }
                                                .mapNotNull { usage -> yarns.find { it.id == usage.yarnId } }
                                        }

                                        if (yarnsForProject.isNotEmpty()) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                yarnsForProject.take(5).forEach { yarn ->
                                                    val yarnImageBytes = yarnImages[yarn.id]
                                                    if (yarnImageBytes != null) {
                                                        val bitmap = remember(yarnImageBytes) { yarnImageBytes.toImageBitmap() }
                                                        Image(
                                                            bitmap = bitmap,
                                                            contentDescription = "Yarn image for ${'$'}{yarn.name}",
                                                            modifier = Modifier.size(32.dp),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    } else {
                                                        Image(
                                                            painter = painterResource(Res.drawable.yarns),
                                                            contentDescription = "Yarn icon",
                                                            modifier = Modifier.size(32.dp).alpha(0.5f)
                                                        )
                                                    }
                                                }



                                            }
                                        }





                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
