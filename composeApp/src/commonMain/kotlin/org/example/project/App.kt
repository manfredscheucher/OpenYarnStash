package org.example.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import openyarnstash.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
//import java.util.Locale // TODO
import kotlin.NoSuchElementException // Ensure this import is present
import kotlin.random.Random

sealed class Screen {
    data object Home : Screen()
    data object YarnList : Screen()
    data class YarnForm(val yarnId: Int) : Screen() // yarnId is no longer nullable
    data object ProjectList : Screen()
    data class ProjectForm(val projectId: Int) : Screen() // projectId is no longer nullable
    data class ProjectAssignments(val projectId: Int, val projectName: String) : Screen()
    data object Info : Screen()
    data object Statistics : Screen()
    data object Settings : Screen()
}

@Composable
fun App(jsonDataManager: JsonDataManager, imageManager: ImageManager, settingsManager: JsonSettingsManager, fileDownloader: FileDownloader, fileHandler: FileHandler) {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var settings by remember { mutableStateOf(Settings()) }
    var yarns by remember { mutableStateOf(emptyList<Yarn>()) }
    var projects by remember { mutableStateOf(emptyList<Project>()) }
    var usages by remember { mutableStateOf(emptyList<Usage>()) }
    var projectImages by remember { mutableStateOf<Map<Int, ByteArray?>>(emptyMap()) }
    var yarnImages by remember { mutableStateOf<Map<Int, ByteArray?>>(emptyMap()) }
    var showNotImplementedDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val emptyImageByteArray = remember { createEmptyImageByteArray() }

    suspend fun reloadAllData() {
        try {
            val data = withContext(Dispatchers.Default) { jsonDataManager.load() }
            yarns = data.yarns
            projects = data.projects
            usages = data.usages
        } catch (e: Exception) {
            errorDialogMessage = "Failed to load data: ${e.message}"
        }
    }

    LaunchedEffect(Unit) {
        settings = withContext(Dispatchers.Default) { settingsManager.loadSettings() }
        setAppLanguage(settings.language)
        reloadAllData()
    }

    if (showNotImplementedDialog) {
        AlertDialog(
            onDismissRequest = { showNotImplementedDialog = false },
            title = { Text(stringResource(Res.string.not_implemented_title)) },
            text = { Text(stringResource(Res.string.not_implemented_message)) },
            confirmButton = {
                TextButton(onClick = { showNotImplementedDialog = false }) {
                    Text(stringResource(Res.string.common_ok))
                }
            }
        )
    }

    if (errorDialogMessage != null) {
        AlertDialog(
            onDismissRequest = { errorDialogMessage = null },
            title = { Text("Error") },
            text = { Text(errorDialogMessage!!) },
            confirmButton = {
                TextButton(onClick = { errorDialogMessage = null }) {
                    Text(stringResource(Res.string.common_ok))
                }
            }
        )
    }

    MaterialTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            key(settings.language) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (val s = screen) {
                        Screen.Home -> HomeScreen(
                            onOpenYarns = { screen = Screen.YarnList },
                            onOpenProjects = { screen = Screen.ProjectList },
                            onOpenInfo = { screen = Screen.Info },
                            onOpenStatistics = { screen = Screen.Statistics },
                            onOpenSettings = { screen = Screen.Settings }
                        )

                        Screen.YarnList -> {
                            LaunchedEffect(yarns) {
                                yarnImages =
                                    yarns.associate { it.id to withContext(Dispatchers.Default) { imageManager.getYarnImage(it.id) } }
                            }
                            val defaultYarnName = stringResource(Res.string.yarn_new_default_name)
                            YarnListScreen(
                                yarns = yarns.sortedByDescending { it.modified },
                                yarnImages = yarnImages,
                                usages = usages,
                                settings = settings,
                                onAddClick = {
                                    scope.launch {
                                        val existingIds = yarns.map { it.id }.toSet()
                                        var newId: Int
                                        do {
                                            newId = Random.nextInt(1_000_000, 10_000_000)
                                        } while (existingIds.contains(newId))
                                        val yarnName =
                                            defaultYarnName.replace("%1\$d", newId.toString())
                                        val newYarn = Yarn(
                                            id = newId,
                                            name = yarnName,
                                            modified = getCurrentTimestamp()
                                        ) // Default name in English as fallback
                                        withContext(Dispatchers.Default) { jsonDataManager.addOrUpdateYarn(newYarn) }
                                        reloadAllData()
                                        screen = Screen.YarnForm(newId)
                                    }
                                },
                                onOpen = { id -> screen = Screen.YarnForm(id) },
                                onBack = { screen = Screen.Home },
                                onSettingsChange = { newSettings ->
                                    scope.launch {
                                        withContext(Dispatchers.Default) {
                                            settingsManager.saveSettings(newSettings)
                                        }
                                        settings = newSettings
                                    }
                                }
                            )
                        }

                        is Screen.YarnForm -> {
                            val existingYarn = remember(s.yarnId, yarns) {
                                try {
                                    jsonDataManager.getYarnById(s.yarnId)
                                } catch (e: NoSuchElementException) {
                                    null
                                }
                            }
                            var yarnImage by remember { mutableStateOf<ByteArray?>(null) }

                            LaunchedEffect(s.yarnId) {
                                yarnImage = withContext(Dispatchers.Default) {
                                    imageManager.getYarnImage(s.yarnId)
                                }
                            }

                            if (existingYarn == null) {
                                LaunchedEffect(s.yarnId) { screen = Screen.YarnList }
                            } else {
                                val relatedUsages = usages.filter { it.yarnId == existingYarn.id }
                                YarnFormScreen(
                                    initial = existingYarn,
                                    initialImage = yarnImage,
                                    usagesForYarn = relatedUsages,
                                    projectNameById = { pid ->
                                        try {
                                            projects.firstOrNull { it.id == pid }?.name ?: "?"
                                        } catch (e: NoSuchElementException) {
                                            "?"
                                        }
                                    },
                                    onBack = { screen = Screen.YarnList },
                                    onDelete = { yarnIdToDelete ->
                                        scope.launch {
                                            withContext(Dispatchers.Default) { jsonDataManager.deleteYarn(yarnIdToDelete) }
                                            reloadAllData()
                                            screen = Screen.YarnList
                                        }
                                    },
                                    onSave = { editedYarn, image ->
                                        scope.launch {
                                            withContext(Dispatchers.Default) {
                                                jsonDataManager.addOrUpdateYarn(editedYarn)
                                                if (image != null) {
                                                    if (image.contentEquals(emptyImageByteArray)) {
                                                        imageManager.deleteYarnImage(editedYarn.id)
                                                    } else {
                                                        imageManager.saveYarnImage(
                                                            editedYarn.id,
                                                            image
                                                        )
                                                    }
                                                }
                                            }
                                            reloadAllData()
                                            screen = Screen.YarnList
                                        }
                                    },
                                    onSetRemainingToZero = { yarnIdToUpdate, newAmount ->
                                        scope.launch {
                                            jsonDataManager.getYarnById(yarnIdToUpdate)
                                                ?.let { yarnToUpdate ->
                                                    val updatedYarn =
                                                        yarnToUpdate.copy(amount = newAmount)
                                                    withContext(Dispatchers.Default) { jsonDataManager.addOrUpdateYarn(updatedYarn) }
                                                    reloadAllData()
                                                    screen = Screen.YarnList
                                                }
                                        }
                                    }
                                )
                            }
                        }

                        Screen.ProjectList -> {
                            LaunchedEffect(projects) {
                                projectImages =
                                    projects.associate { it.id to withContext(Dispatchers.Default) { imageManager.getProjectImage(it.id) } }
                            }
                            val defaultProjectName =
                                stringResource(Res.string.project_new_default_name)
                            ProjectListScreen(
                                projects = projects.sortedByDescending { it.modified },
                                projectImages = projectImages,
                                settings = settings,
                                onAddClick = {
                                    scope.launch {
                                        val existingIds = projects.map { it.id }.toSet()
                                        var newId: Int
                                        do {
                                            newId = Random.nextInt(1_000_000, 10_000_000)
                                        } while (existingIds.contains(newId))
                                        val projectName =
                                            defaultProjectName.replace("%1\$d", newId.toString())
                                        val newProject = Project(
                                            id = newId,
                                            name = projectName,
                                            modified = getCurrentTimestamp()
                                        ) // Default name
                                        withContext(Dispatchers.Default) { jsonDataManager.addOrUpdateProject(newProject) }
                                        reloadAllData()
                                        screen = Screen.ProjectForm(newId)
                                    }
                                },
                                onOpen = { id -> screen = Screen.ProjectForm(id) },
                                onBack = { screen = Screen.Home },
                                onSettingsChange = { newSettings ->
                                    scope.launch {
                                        withContext(Dispatchers.Default) {
                                            settingsManager.saveSettings(newSettings)
                                        }
                                        settings = newSettings
                                    }
                                }
                            )
                        }

                        is Screen.ProjectForm -> {
                            val existingProject = remember(s.projectId, projects) {
                                try {
                                    jsonDataManager.getProjectById(s.projectId)
                                } catch (e: NoSuchElementException) {
                                    null
                                }
                            }
                            var projectImage by remember { mutableStateOf<ByteArray?>(null) }

                            LaunchedEffect(s.projectId) {
                                projectImage = withContext(Dispatchers.Default) {
                                    imageManager.getProjectImage(s.projectId)
                                }
                            }

                            if (existingProject == null) {
                                LaunchedEffect(s.projectId) { screen = Screen.ProjectList }
                            } else {
                                val usagesForCurrentProject =
                                    usages.filter { it.projectId == existingProject.id }
                                ProjectFormScreen(
                                    initial = existingProject,
                                    initialImage = projectImage,
                                    usagesForProject = usagesForCurrentProject,
                                    yarnById = { yarnId -> yarns.firstOrNull { it.id == yarnId } },
                                    onBack = { screen = Screen.ProjectList },
                                    onDelete = { id ->
                                        scope.launch {
                                            withContext(Dispatchers.Default) { jsonDataManager.deleteProject(id) }
                                            reloadAllData()
                                            screen = Screen.ProjectList
                                        }
                                    },
                                    onSave = { editedProject, image ->
                                        scope.launch {
                                            withContext(Dispatchers.Default) {
                                                jsonDataManager.addOrUpdateProject(editedProject)
                                                if (image != null) {
                                                    if (image.contentEquals(emptyImageByteArray)) {
                                                        imageManager.deleteProjectImage(editedProject.id)
                                                    } else {
                                                        imageManager.saveProjectImage(
                                                            editedProject.id,
                                                            image
                                                        )
                                                    }
                                                }
                                            }
                                            reloadAllData()
                                            screen = Screen.ProjectList
                                        }
                                    },
                                    onNavigateToAssignments = {
                                        screen = Screen.ProjectAssignments(
                                            existingProject.id,
                                            existingProject.name
                                        )
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
                                        jsonDataManager.availableForYarn(
                                            yarnId,
                                            forProjectId = s.projectId
                                        )
                                    } catch (e: NoSuchElementException) {
                                        0
                                    }
                                },
                                onSave = { updatedAssignments ->
                                    scope.launch {
                                        withContext(Dispatchers.Default) {
                                            jsonDataManager.setProjectAssignments(
                                                s.projectId,
                                                updatedAssignments
                                            )
                                        }
                                        reloadAllData()
                                        screen = Screen.ProjectForm(s.projectId)
                                    }
                                },
                                onBack = { screen = Screen.ProjectForm(s.projectId) }
                            )
                        }

                        Screen.Info -> {
                            InfoScreen(onBack = { screen = Screen.Home })
                        }

                        Screen.Statistics -> {
                            StatisticsScreen(
                                yarns = yarns, usages = usages, onBack = { screen = Screen.Home },
                                projects = projects
                            )
                        }

                        Screen.Settings -> {
                            key(settings.language) {
                                SettingsScreen(
                                    currentLocale = settings.language,
                                    onBack = { screen = Screen.Home },
                                    onExport = {
                                        scope.launch {
                                            withContext(Dispatchers.Default) { jsonDataManager.backup() } // Best-effort backup
                                            val json = withContext(Dispatchers.Default) { jsonDataManager.getRawJson() }
                                            val exportFileName = fileHandler.createTimestampedFileName("open-yarn-stash", "json")
                                            fileDownloader.download(exportFileName, json)
                                        }
                                    },
                                    onImport = { fileContent ->
                                        scope.launch {
                                            try {
                                                withContext(Dispatchers.Default) {
                                                    jsonDataManager.importData(fileContent)
                                                }
                                                reloadAllData()
                                                snackbarHostState.showSnackbar("Import erfolgreich")
                                            } catch (e: Exception) {
                                                errorDialogMessage = "Failed to import data: ${e.message}"
                                            }
                                        }
                                    },
                                    onLocaleChange = { newLocale ->
                                        scope.launch {
                                            val newSettings = settings.copy(language = newLocale)
                                            withContext(Dispatchers.Default) {
                                                settingsManager.saveSettings(newSettings)
                                            }
                                            setAppLanguage(newLocale)
                                            settings = newSettings
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
