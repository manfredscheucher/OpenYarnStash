package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import knittingappmultiplatt.composeapp.generated.resources.*
import kotlin.NoSuchElementException // Ensure this import is present

sealed class Screen {
    data object Home : Screen()
    data object YarnList : Screen()
    data class YarnForm(val yarnId: Int) : Screen() // yarnId is no longer nullable
    data object ProjectList : Screen()
    data class ProjectForm(val projectId: Int) : Screen() // projectId is no longer nullable
    data class ProjectAssignments(val projectId: Int, val projectName: String) : Screen()
    data object Info : Screen()
    data object Statistics : Screen()
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
                onOpenProjects = { screen = Screen.ProjectList },
                onOpenInfo = { screen = Screen.Info },
                onOpenStatistics = { screen = Screen.Statistics }
            )

            Screen.YarnList -> {
                YarnListScreen(
                    yarns = yarns,
                    usages = usages,
                    onAddClick = {
                        scope.launch {
                            val newId = repo.nextYarnId()
                            val newYarn = Yarn(id = newId, name = "New Yarn ($newId)") // Default name in English as fallback
                            withContext(Dispatchers.Default) { repo.addOrUpdateYarn(newYarn) }
                            reloadAllData()
                            screen = Screen.YarnForm(newId)
                        }
                    },
                    onOpen = { id -> screen = Screen.YarnForm(id) },
                    onBack = { screen = Screen.Home }
                )
            }

            is Screen.YarnForm -> {
                val existingYarn = remember(s.yarnId, yarns) {
                    try { repo.getYarnById(s.yarnId) } catch (e: NoSuchElementException) { null }
                }

                if (existingYarn == null) {
                    LaunchedEffect(s.yarnId) { screen = Screen.YarnList }
                } else {
                    val relatedUsages = usages.filter { it.yarnId == existingYarn.id }
                    YarnFormScreen(
                        initial = existingYarn,
                        usagesForYarn = relatedUsages,
                        projectNameById = { pid ->
                            try { projects.firstOrNull { it.id == pid }?.name ?: "?" } 
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
                            scope.launch {
                                withContext(Dispatchers.Default) { repo.addOrUpdateYarn(editedYarn) }
                                reloadAllData()
                                screen = Screen.YarnList
                            }
                        },
                        onSetRemainingToZero = { yarnIdToUpdate, newAmount ->
                            scope.launch {
                                val yarnToUpdate = repo.getYarnById(yarnIdToUpdate)
                                val updatedYarn = yarnToUpdate.copy(amount = newAmount)
                                withContext(Dispatchers.Default) { repo.addOrUpdateYarn(updatedYarn) }
                                reloadAllData()
                                screen = Screen.YarnList
                            }
                        }
                    )
                }
            }

            Screen.ProjectList -> {
                ProjectListScreen(
                    projects = projects,
                    onAddClick = {
                        scope.launch {
                            val newId = repo.nextProjectId()
                            val newProject = Project(id = newId, name = "New Project ($newId)") // Default name
                            withContext(Dispatchers.Default) { repo.addOrUpdateProject(newProject) }
                            reloadAllData()
                            screen = Screen.ProjectForm(newId)
                        }
                    },
                    onOpen = { id -> screen = Screen.ProjectForm(id) },
                    onBack = { screen = Screen.Home }
                )
            }

            is Screen.ProjectForm -> {
                val existingProject = remember(s.projectId, projects) {
                    try { repo.getProjectById(s.projectId) } catch (e: NoSuchElementException) { null }
                }

                if (existingProject == null) {
                    LaunchedEffect(s.projectId) { screen = Screen.ProjectList }
                } else {
                    val usagesForCurrentProject = usages.filter { it.projectId == existingProject.id }
                    ProjectFormScreen(
                        initial = existingProject,
                        usagesForProject = usagesForCurrentProject, 
                        yarnNameById = { yarnId -> 
                            try { yarns.firstOrNull { it.id == yarnId }?.name ?: "?" }
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
                            scope.launch {
                                withContext(Dispatchers.Default) { repo.addOrUpdateProject(editedProject) }
                                reloadAllData()
                                screen = Screen.ProjectList
                            }
                        },
                        onNavigateToAssignments = {
                            screen = Screen.ProjectAssignments(existingProject.id, existingProject.name)
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
            Screen.Info -> {
                InfoScreen(onBack = { screen = Screen.Home })
            }
            Screen.Statistics -> {
                StatisticsScreen(onBack = { screen = Screen.Home })
            }
        }
    }
}
