package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.NoSuchElementException // Ensure this import is present

sealed class Screen {
    data object Home : Screen()
    data object YarnList : Screen()
    data class YarnForm(val yarnId: Int?) : Screen()
    data object ProjectList : Screen()
    data class ProjectForm(val projectId: Int?) : Screen()
    data class ProjectAssignments(val projectId: Int, val projectName: String) : Screen()
}

@Composable
fun App(repo: JsonRepository) {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    var yarns by remember { mutableStateOf(emptyList<Yarn>()) }
    var projects by remember { mutableStateOf(emptyList<Project>()) }
    var usages by remember { mutableStateOf(emptyList<Usage>()) }

    val scope = rememberCoroutineScope()

    suspend fun reloadAllData() {
        val data = withContext(Dispatchers.Default) { repo.load() }
        yarns = data.yarns
        projects = data.projects
        usages = data.usages
    }

    LaunchedEffect(Unit) {
        reloadAllData()
    }

    MaterialTheme {
        when (val s = screen) {
            Screen.Home -> HomeScreen(
                onOpenYarns = { screen = Screen.YarnList },
                onOpenProjects = { screen = Screen.ProjectList }
            )

            Screen.YarnList -> YarnListScreen(
                yarns = yarns,
                usages = usages,
                onAddClick = { screen = Screen.YarnForm(null) },
                onOpen = { id -> screen = Screen.YarnForm(id) },
                onBack = { screen = Screen.Home }
            )

            is Screen.YarnForm -> {
                val existingYarn = remember(s.yarnId, yarns) {
                    s.yarnId?.let { id -> 
                        try { repo.getYarnById(id) } catch (e: NoSuchElementException) { null }
                    }
                }

                if (s.yarnId != null && existingYarn == null) {
                    // Yarn not found, navigate back or show error
                    LaunchedEffect(s.yarnId) { screen = Screen.YarnList }
                } else {
                    val relatedUsages = existingYarn?.id?.let { id -> usages.filter { it.yarnId == id } } ?: emptyList()
                    YarnFormScreen(
                        initial = existingYarn,
                        usagesForYarn = relatedUsages,
                        projectNameById = { pid ->
                            try { repo.getProjectById(pid).name } catch (e: NoSuchElementException) { "?" }
                        },
                        onCancel = { screen = Screen.YarnList },
                        onDelete = { id ->
                            scope.launch {
                                withContext(Dispatchers.Default) { repo.deleteYarn(id) }
                                reloadAllData()
                                screen = Screen.YarnList
                            }
                        },
                        onSave = { edited ->
                            val toSave = edited.copy(
                                id = edited.id.takeIf { it > 0 } ?: repo.nextYarnId()
                            )
                            scope.launch {
                                withContext(Dispatchers.Default) { repo.addOrUpdateYarn(toSave) }
                                reloadAllData()
                                screen = Screen.YarnList
                            }
                        }
                    )
                }
            }

            Screen.ProjectList -> ProjectListScreen(
                projects = projects,
                onAddClick = { screen = Screen.ProjectForm(null) },
                onOpen = { id -> screen = Screen.ProjectForm(id) },
                onBack = { screen = Screen.Home }
            )

            is Screen.ProjectForm -> {
                val existingProject = remember(s.projectId, projects) {
                    s.projectId?.let { id ->
                        try { repo.getProjectById(id) } catch (e: NoSuchElementException) { null }
                    }
                }

                if (s.projectId != null && existingProject == null) {
                    // Project not found, navigate back
                    LaunchedEffect(s.projectId) { screen = Screen.ProjectList }
                } else {
                    ProjectFormScreen(
                        initial = existingProject,
                        onCancel = { screen = Screen.ProjectList },
                        onDelete = { id ->
                            scope.launch {
                                withContext(Dispatchers.Default) { repo.deleteProject(id) }
                                reloadAllData()
                                screen = Screen.ProjectList
                            }
                        },
                        onSave = { editedProject ->
                            val toSave = editedProject.copy(
                                id = editedProject.id.takeIf { it > 0 } ?: repo.nextProjectId()
                            )
                            scope.launch {
                                val savedProject = withContext(Dispatchers.Default) { repo.addOrUpdateProject(toSave) }.projects.find { it.id == toSave.id } ?: toSave
                                reloadAllData() // Reload to get consistent data including new project ID if it was new
                                if (s.projectId == null) { // New project was saved
                                    screen = Screen.ProjectAssignments(savedProject.id, savedProject.name)
                                } else { // Existing project was saved
                                    screen = Screen.ProjectList // Or back to ProjectForm(s.projectId)
                                }
                            }
                        },
                        onNavigateToAssignments = {
                            existingProject?.let { proj ->
                                screen = Screen.ProjectAssignments(proj.id, proj.name)
                            }
                        }
                    )
                }
            }

            is Screen.ProjectAssignments -> {
                val initialAssignmentsForProject = usages
                    .filter { it.projectId == s.projectId }
                    .associate { it.yarnId to it.amount }

                ProjectAssignmentsScreen(
                    projectName = s.projectName,
                    allYarns = yarns,
                    initialAssignments = initialAssignmentsForProject,
                    getAvailableAmountForYarn = { yarnId ->
                        try {
                            repo.availableForYarn(yarnId, forProjectId = s.projectId)
                        } catch (e: NoSuchElementException) {
                            0 // Should not happen if allYarns is consistent with repo cache
                        }
                    },
                    onSave = { updatedAssignments ->
                        scope.launch {
                            withContext(Dispatchers.Default) { repo.setProjectAssignments(s.projectId, updatedAssignments) }
                            reloadAllData()
                            // Navigate back to the ProjectForm for the current project
                            screen = Screen.ProjectForm(s.projectId) 
                        }
                    },
                    onCancel = { 
                        // Navigate back to the ProjectForm for the current project
                        screen = Screen.ProjectForm(s.projectId) 
                    }
                )
            }
        }
    }
}
