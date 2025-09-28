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
                    LaunchedEffect(s.yarnId) { screen = Screen.YarnList }
                } else {
                    val relatedUsages = existingYarn?.id?.let { id -> usages.filter { it.yarnId == id } } ?: emptyList()
                    YarnFormScreen(
                        initial = existingYarn,
                        usagesForYarn = relatedUsages,
                        projectNameById = { pid ->
                            try { projects.firstOrNull { it.id == pid }?.name ?: repo.getProjectById(pid).name } 
                            catch (e: NoSuchElementException) { "?" }
                        },
                        onCancel = { screen = Screen.YarnList },
                        onDelete = { yarnIdToDelete ->
                            scope.launch {
                                withContext(Dispatchers.Default) { repo.deleteYarn(yarnIdToDelete) }
                                reloadAllData()
                                screen = Screen.YarnList
                            }
                        },
                        onSave = { editedYarn ->
                            val yarnToSave = editedYarn.copy(
                                id = editedYarn.id.takeIf { it > 0 } ?: repo.nextYarnId()
                            )
                            scope.launch {
                                withContext(Dispatchers.Default) { repo.addOrUpdateYarn(yarnToSave) }
                                reloadAllData()
                                screen = Screen.YarnList
                            }
                        },
                        onSetRemainingToZero = { yarnIdToUpdate, newAmount ->
                            scope.launch {
                                try {
                                    val yarnToUpdate = repo.getYarnById(yarnIdToUpdate)
                                    val updatedYarn = yarnToUpdate.copy(amount = newAmount)
                                    withContext(Dispatchers.Default) { repo.addOrUpdateYarn(updatedYarn) }
                                    reloadAllData()
                                    screen = Screen.YarnList
                                } catch (e: NoSuchElementException) {
                                    // Handle case where yarn might have been deleted in the meantime, though unlikely here
                                    reloadAllData() // Reload to be safe
                                    screen = Screen.YarnList 
                                }
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
                    LaunchedEffect(s.projectId) { screen = Screen.ProjectList }
                } else {
                    val usagesForCurrentProject = existingProject?.id?.let { pid -> 
                        usages.filter { it.projectId == pid } 
                    } ?: emptyList()

                    ProjectFormScreen(
                        initial = existingProject,
                        usagesForProject = usagesForCurrentProject, 
                        yarnNameById = { yarnId -> 
                            try { yarns.firstOrNull { it.id == yarnId }?.name ?: repo.getYarnById(yarnId).name }
                            catch (e: NoSuchElementException) { "?" }
                        },
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
                                val savedProjectWithData = withContext(Dispatchers.Default) { repo.addOrUpdateProject(toSave) }
                                val finalProject = savedProjectWithData.projects.find { it.id == toSave.id } ?: 
                                                   (if (toSave.id == -1 && savedProjectWithData.projects.isNotEmpty()) savedProjectWithData.projects.last() else toSave)

                                reloadAllData()
                                
                                if (s.projectId == null) { 
                                    screen = Screen.ProjectAssignments(finalProject.id, finalProject.name)
                                } else { 
                                    screen = Screen.ProjectList 
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
                            0 
                        }
                    },
                    onSave = { updatedAssignments ->
                        scope.launch {
                            withContext(Dispatchers.Default) { repo.setProjectAssignments(s.projectId, updatedAssignments) }
                            reloadAllData()
                            screen = Screen.ProjectForm(s.projectId) 
                        }
                    },
                    onCancel = { 
                        screen = Screen.ProjectForm(s.projectId) 
                    }
                )
            }
        }
    }
}
