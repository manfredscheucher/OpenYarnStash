package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers // Added import
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun App(repo: JsonRepository) {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    var yarns by remember { mutableStateOf(emptyList<Yarn>()) }
    var projects by remember { mutableStateOf(emptyList<Project>()) }
    var usages by remember { mutableStateOf(emptyList<Usage>()) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val data = withContext(Dispatchers.Default) { repo.load() }
        yarns = data.yarns
        projects = data.projects
        usages = data.usages
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
                val existing = s.yarnId?.let { id -> yarns.firstOrNull { it.id == id } }
                val relatedUsages = existing?.id?.let { id -> usages.filter { it.yarnId == id } } ?: emptyList()

                YarnFormScreen(
                    initial = existing,
                    usagesForYarn = relatedUsages,
                    projectNameById = { pid -> projects.firstOrNull { it.id == pid }?.name ?: "?" },
                    onCancel = { screen = Screen.YarnList },
                    onDelete = { id ->
                        scope.launch {
                            val updated = withContext(Dispatchers.Default) { repo.deleteYarn(id) }
                            yarns = updated.yarns
                            usages = updated.usages
                            screen = Screen.YarnList
                        }
                    },
                    onSave = { edited ->
                        val toSave = edited.copy(
                            id = edited.id.takeIf { it > 0 } ?: (yarns.maxOfOrNull { it.id } ?: 0) + 1
                        )
                        scope.launch {
                            val updated = withContext(Dispatchers.Default) { repo.addOrUpdateYarn(toSave) }
                            yarns = updated.yarns
                            // Reload usages as addOrUpdateYarn doesn't return them directly in this version
                            val re = withContext(Dispatchers.Default) { repo.load() }
                            usages = re.usages
                            screen = Screen.YarnList
                        }
                    }
                )
            }

            Screen.ProjectList -> ProjectListScreen(
                projects = projects,
                onAddClick = { screen = Screen.ProjectForm(null) },
                onOpen = { id -> screen = Screen.ProjectForm(id) },
                onBack = { screen = Screen.Home }
            )

            is Screen.ProjectForm -> {
                val existing = s.projectId?.let { id -> projects.firstOrNull { it.id == id } }
                val currentProjectId = existing?.id ?: -1 // Used for availableForYarn calculation
                val assignmentsMap: Map<Int, Int> =
                    if (existing != null)
                        usages.filter { it.projectId == existing.id }.associate { it.yarnId to it.amount }
                    else emptyMap()

                ProjectFormScreen(
                    initial = existing,
                    yarns = yarns, // Pass all yarns for selection
                    currentAssignments = assignmentsMap,
                    availableForYarn = { yarnId -> repo.availableForYarn(yarnId, forProjectId = currentProjectId) },
                    onCancel = { screen = Screen.ProjectList },
                    onDelete = { id ->
                        scope.launch {
                            val updated = withContext(Dispatchers.Default) { repo.deleteProject(id) }
                            projects = updated.projects
                            usages = updated.usages
                            screen = Screen.ProjectList
                        }
                    },
                    onSave = { editedProject, newAssignments ->
                        val toSave = editedProject.copy(
                            id = editedProject.id.takeIf { it > 0 } ?: (projects.maxOfOrNull { it.id } ?: 0) + 1
                        )
                        scope.launch {
                            // 1) save project details
                            withContext(Dispatchers.Default) { repo.addOrUpdateProject(toSave) }
                            // 2) enforce uniqueness & set all assignments atomically for this project
                            withContext(Dispatchers.Default) { repo.setProjectAssignments(toSave.id, newAssignments) }
                            // 3) reload all data to reflect changes
                            val reloadedData = withContext(Dispatchers.Default) { repo.load() }
                            projects = reloadedData.projects
                            yarns = reloadedData.yarns // yarns might not change here, but good practice
                            usages = reloadedData.usages
                            screen = Screen.ProjectList
                        }
                    }
                )
            }
        }
    }
}
