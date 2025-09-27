package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun App(repo: JsonRepository) {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    var yarns by remember { mutableStateOf(emptyList<Yarn>()) }
    var projects by remember { mutableStateOf(emptyList<Project>()) }
    // usages lassen wir für später (Verknüpfung)

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val data = withContext(Dispatchers.Default) { repo.load() }
        yarns = data.yarns
        projects = data.projects
    }

    MaterialTheme {
        when (val s = screen) {

            Screen.Home -> HomeScreen(
                onOpenYarns = { screen = Screen.YarnList },
                onOpenProjects = { screen = Screen.ProjectList }
            )

            // ------- Yarns (dein bestehender Code, hier kurz) -------
            Screen.YarnList -> YarnListScreen(
                yarns = yarns,
                onAddClick = { screen = Screen.YarnForm(null) },
                onOpen = { id -> screen = Screen.YarnForm(id) },
                onBack = { screen = Screen.Home }
            )
            is Screen.YarnForm -> {
                val existing = s.yarnId?.let { id -> yarns.firstOrNull { it.id == id } }
                YarnFormScreen(
                    initial = existing,
                    onCancel = { screen = Screen.YarnList },
                    onDelete = { id ->
                        scope.launch {
                            val updated = withContext(Dispatchers.Default) { repo.deleteYarn(id) }
                            yarns = updated.yarns
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
                            screen = Screen.YarnList
                        }
                    }
                )
            }

            // ------- Projects -------
            Screen.ProjectList -> ProjectListScreen(
                projects = projects,
                onAddClick = { screen = Screen.ProjectForm(null) },
                onOpen = { id -> screen = Screen.ProjectForm(id) },
                onBack = { screen = Screen.Home }
            )
            is Screen.ProjectForm -> {
                val existing = s.projectId?.let { id -> projects.firstOrNull { it.id == id } }
                ProjectFormScreen(
                    initial = existing,
                    onCancel = { screen = Screen.ProjectList },
                    onDelete = { id ->
                        scope.launch {
                            val updated = withContext(Dispatchers.Default) { repo.deleteProject(id) }
                            projects = updated.projects
                            screen = Screen.ProjectList
                        }
                    },
                    onSave = { edited ->
                        val toSave = edited.copy(
                            id = edited.id.takeIf { it > 0 } ?: (projects.maxOfOrNull { it.id } ?: 0) + 1
                        )
                        scope.launch {
                            val updated = withContext(Dispatchers.Default) { repo.addOrUpdateProject(toSave) }
                            projects = updated.projects
                            screen = Screen.ProjectList
                        }
                    }
                )
            }
        }
    }
}

private fun JsonRepository.deleteProject(id: Int) {}
