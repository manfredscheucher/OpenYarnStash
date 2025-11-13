package org.example.project

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
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

sealed class Screen {
    data object Home : Screen()
    data object YarnList : Screen()
    data class YarnForm(val yarnId: Int) : Screen() // yarnId is no longer nullable
    data object ProjectList : Screen()
    data class ProjectForm(val projectId: Int) : Screen() // projectId is no longer nullable
    data class ProjectAssignments(val projectId: Int, val projectName: String) : Screen()
    data object Info : Screen()
    data object HowToHelp : Screen()
    data object Statistics : Screen()
    data object Settings : Screen()
    data object PatternList : Screen()
    data class PatternForm(val patternId: Int) : Screen()
}

@Composable
fun App(jsonDataManager: JsonDataManager, imageManager: ImageManager, pdfManager: PdfManager, settingsManager: JsonSettingsManager, fileDownloader: FileDownloader, fileHandler: FileHandler) {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var settings by remember { mutableStateOf(Settings()) }
    var yarns by remember { mutableStateOf(emptyList<Yarn>()) }
    var projects by remember { mutableStateOf(emptyList<Project>()) }
    var usages by remember { mutableStateOf(emptyList<Usage>()) }
    var patterns by remember { mutableStateOf(emptyList<Pattern>()) }
    var showNotImplementedDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    suspend fun reloadAllData() {
        try {
            val data = withContext(Dispatchers.Default) { jsonDataManager.load() }
            yarns = data.yarns
            projects = data.projects
            usages = data.usages
            patterns = data.patterns
        } catch (e: Exception) {
            errorDialogMessage = "Failed to load data: ${e.message}. The data file might be corrupt."
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

    MaterialTheme(
        colorScheme = LightColorScheme
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets
        ) { innerPadding ->
            key(settings.language, settings.lengthUnit) {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                ) {
                    when (val s = screen) {
                        Screen.Home -> HomeScreen(
                            onOpenYarns = { screen = Screen.YarnList },
                            onOpenProjects = { screen = Screen.ProjectList },
                            onOpenPatterns = { screen = Screen.PatternList },
                            onOpenInfo = { screen = Screen.Info },
                            onOpenStatistics = { screen = Screen.Statistics },
                            onOpenSettings = { screen = Screen.Settings },
                            onOpenHowToHelp = { screen = Screen.HowToHelp }
                        )

                        Screen.YarnList -> {
                            val defaultYarnName = stringResource(Res.string.yarn_new_default_name)
                            YarnListScreen(
                                yarns = yarns.sortedByDescending { it.modified },
                                imageManager = imageManager,
                                usages = usages,
                                settings = settings,
                                onAddClick = {
                                    scope.launch {
                                        val newYarn = jsonDataManager.createNewYarn(defaultYarnName)
                                        withContext(Dispatchers.Default) { jsonDataManager.addOrUpdateYarn(newYarn) }
                                        reloadAllData()
                                        screen = Screen.YarnForm(newYarn.id)
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
                            var yarnImagesMap by remember { mutableStateOf<Map<Int, ByteArray>>(emptyMap()) }

                            LaunchedEffect(s.yarnId, existingYarn) {
                                yarnImagesMap = existingYarn?.imageIds?.associateWith { imageId ->
                                    withContext(Dispatchers.Default) {
                                        imageManager.getYarnImage(existingYarn.id, imageId)
                                    }
                                }?.filterValues { it != null }?.mapValues { it.value!! } ?: emptyMap()
                            }

                            if (existingYarn == null) {
                                LaunchedEffect(s.yarnId) { screen = Screen.YarnList }
                            } else {
                                val relatedUsages = usages.filter { it.yarnId == existingYarn.id }
                                YarnFormScreen(
                                    initial = existingYarn,
                                    initialImages = yarnImagesMap,
                                    usagesForYarn = relatedUsages,
                                    projectById = { pid -> projects.firstOrNull { it.id == pid } },
                                    imageManager = imageManager,
                                    settings = settings,
                                    onBack = { screen = Screen.YarnList },
                                    onDelete = { yarnIdToDelete ->
                                        scope.launch {
                                            withContext(Dispatchers.Default) {
                                                val yarnToDelete = jsonDataManager.getYarnById(yarnIdToDelete)
                                                yarnToDelete?.imageIds?.forEach { imageId ->
                                                    imageManager.deleteYarnImage(yarnIdToDelete, imageId)
                                                }
                                                jsonDataManager.deleteYarn(yarnIdToDelete)
                                            }
                                            reloadAllData()
                                            screen = Screen.YarnList
                                        }
                                    },
                                    onSave = { editedYarn, newImages ->
                                        scope.launch {
                                            val existingImageIds = existingYarn.imageIds
                                            val keptImageIds = editedYarn.imageIds.filter { it > 0 }
                                            val idsToDelete = existingImageIds.filter { it !in keptImageIds }

                                            withContext(Dispatchers.Default) {
                                                idsToDelete.forEach { imageId ->
                                                    imageManager.deleteYarnImage(editedYarn.id, imageId)
                                                }

                                                val finalImageIds = keptImageIds.toMutableList()
                                                var nextImageId = (existingImageIds.maxOrNull() ?: 0) + 1
                                                newImages.keys.sorted().forEach { imageKey ->
                                                    newImages[imageKey]?.let { imageData ->
                                                        imageManager.saveYarnImage(
                                                            editedYarn.id,
                                                            nextImageId,
                                                            imageData
                                                        )
                                                        finalImageIds.add(nextImageId)
                                                        nextImageId++
                                                    }
                                                }

                                                val finalYarn = editedYarn.copy(imageIds = finalImageIds)
                                                jsonDataManager.addOrUpdateYarn(finalYarn)
                                            }
                                            reloadAllData()
                                            screen = Screen.YarnList
                                        }
                                    },
                                    onAddColor = { yarnToCopy ->
                                        scope.launch {
                                            val newYarnWithNewId = jsonDataManager.createNewYarn(yarnToCopy.name)
                                            val newYarn = newYarnWithNewId.copy(
                                                brand = yarnToCopy.brand,
                                                blend = yarnToCopy.blend,
                                                meteragePerSkein = yarnToCopy.meteragePerSkein,
                                                weightPerSkein = yarnToCopy.weightPerSkein
                                            )
                                            withContext(Dispatchers.Default) { jsonDataManager.addOrUpdateYarn(newYarn) }
                                            reloadAllData()
                                            screen = Screen.YarnForm(newYarn.id)
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
                            val defaultProjectName =
                                stringResource(Res.string.project_new_default_name)
                            ProjectListScreen(
                                projects = projects.sortedByDescending { it.modified },
                                imageManager = imageManager,
                                settings = settings,
                                onAddClick = {
                                    scope.launch {
                                        val newProject =
                                            jsonDataManager.createNewProject(defaultProjectName)
                                        withContext(Dispatchers.Default) {
                                            jsonDataManager.addOrUpdateProject(
                                                newProject
                                            )
                                        }
                                        reloadAllData()
                                        screen = Screen.ProjectForm(newProject.id)
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
                                },
                                yarns = yarns,
                                usages = usages
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
                            var projectImagesMap by remember { mutableStateOf<Map<Int, ByteArray>>(emptyMap()) }

                            LaunchedEffect(s.projectId, existingProject) {
                                projectImagesMap = existingProject?.imageIds?.associateWith { imageId ->
                                    withContext(Dispatchers.Default) {
                                        imageManager.getProjectImage(existingProject.id, imageId)
                                    }
                                }?.filterValues { it != null }?.mapValues { it.value!! } ?: emptyMap()
                            }

                            if (existingProject == null) {
                                LaunchedEffect(s.projectId) { screen = Screen.ProjectList }
                            } else {
                                val usagesForCurrentProject =
                                    usages.filter { it.projectId == existingProject.id }
                                ProjectFormScreen(
                                    initial = existingProject,
                                    initialImages = projectImagesMap,
                                    usagesForProject = usagesForCurrentProject,
                                    yarnById = { yarnId -> yarns.firstOrNull { it.id == yarnId } },
                                    patterns = patterns,
                                    imageManager = imageManager,
                                    onBack = { screen = Screen.ProjectList },
                                    onDelete = { id ->
                                        scope.launch {
                                            withContext(Dispatchers.Default) {
                                                val projectToDelete = jsonDataManager.getProjectById(id)
                                                projectToDelete?.imageIds?.forEach { imageId ->
                                                    imageManager.deleteProjectImage(id, imageId)
                                                }
                                                jsonDataManager.deleteProject(id)
                                            }
                                            reloadAllData()
                                            screen = Screen.ProjectList
                                        }
                                    },
                                    onSave = { editedProject, newImages ->
                                        scope.launch {
                                            val existingImageIds = existingProject.imageIds
                                            val keptImageIds = editedProject.imageIds.filter { it > 0 }
                                            val idsToDelete = existingImageIds.filter { it !in keptImageIds }

                                            withContext(Dispatchers.Default) {
                                                idsToDelete.forEach { imageId ->
                                                    imageManager.deleteProjectImage(editedProject.id, imageId)
                                                }

                                                val finalImageIds = keptImageIds.toMutableList()
                                                var nextImageId = (existingImageIds.maxOrNull() ?: 0) + 1
                                                newImages.keys.sorted().forEach { imageKey ->
                                                    newImages[imageKey]?.let { imageData ->
                                                        imageManager.saveProjectImage(
                                                            editedProject.id,
                                                            nextImageId,
                                                            imageData
                                                        )
                                                        finalImageIds.add(nextImageId)
                                                        nextImageId++
                                                    }
                                                }

                                                val finalProject = editedProject.copy(imageIds = finalImageIds)
                                                jsonDataManager.addOrUpdateProject(finalProject)
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
                                    },
                                    onNavigateToPattern = { patternId ->
                                        screen = Screen.PatternForm(patternId)
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

                        Screen.PatternList -> {
                            PatternListScreen(
                                patterns = patterns,
                                onAddClick = {
                                    scope.launch {
                                        val newPattern = jsonDataManager.createNewPattern()
                                        withContext(Dispatchers.Default) { jsonDataManager.addOrUpdatePattern(newPattern) }
                                        reloadAllData()
                                        screen = Screen.PatternForm(newPattern.id)
                                    }
                                },
                                onOpen = { id -> screen = Screen.PatternForm(id) },
                                onBack = { screen = Screen.Home }
                            )
                        }

                        is Screen.PatternForm -> {
                            val existingPattern = remember(s.patternId, patterns) {
                                try {
                                    jsonDataManager.getPatternById(s.patternId)
                                } catch (e: NoSuchElementException) {
                                    null
                                }
                            }

                            var initialPdf by remember { mutableStateOf<ByteArray?>(null) }

                            LaunchedEffect(s.patternId, existingPattern) {
                                initialPdf = if (existingPattern != null) {
                                    withContext(Dispatchers.Default) {
                                        pdfManager.getPatternPdf(existingPattern.id)
                                    }
                                } else {
                                    null
                                }
                            }

                            if (existingPattern == null) {
                                LaunchedEffect(s.patternId) { screen = Screen.PatternList }
                            } else {
                                PatternFormScreen(
                                    initial = existingPattern,
                                    initialPdf = initialPdf,
                                    onBack = { screen = Screen.PatternList },
                                    onDelete = { patternIdToDelete ->
                                        scope.launch {
                                            projects.filter { it.patternId == patternIdToDelete }.forEach { projectToUpdate ->
                                                val updatedProject = projectToUpdate.copy(patternId = null)
                                                withContext(Dispatchers.Default) { jsonDataManager.addOrUpdateProject(updatedProject) }
                                            }
                                            withContext(Dispatchers.Default) {
                                                pdfManager.deletePatternPdf(patternIdToDelete)
                                                jsonDataManager.deletePattern(patternIdToDelete)
                                            }
                                            reloadAllData()
                                            screen = Screen.PatternList
                                        }
                                    },
                                    onSave = { editedPattern, pdf ->
                                        scope.launch {
                                            withContext(Dispatchers.Default) {
                                                if (pdf != null) {
                                                    pdfManager.savePatternPdf(editedPattern.id, pdf)
                                                } else {
                                                    pdfManager.deletePatternPdf(editedPattern.id)
                                                }
                                                jsonDataManager.addOrUpdatePattern(editedPattern)
                                            }
                                            reloadAllData()
                                            screen = Screen.PatternList
                                        }
                                    }
                                )
                            }
                        }

                        Screen.Info -> {
                            InfoScreen(onBack = { screen = Screen.Home }, onNavigateToHelp = { screen = Screen.HowToHelp })
                        }

                        Screen.HowToHelp -> {
                            HowToHelpScreen(onBack = { screen = Screen.Home })
                        }

                        Screen.Statistics -> {
                            StatisticsScreen(
                                yarns = yarns,
                                projects = projects,
                                usages = usages,
                                onBack = { screen = Screen.Home },
                                settings = settings
                            )
                        }

                        Screen.Settings -> {
                            key(settings.language, settings.lengthUnit) {
                                SettingsScreen(
                                    currentLocale = settings.language,
                                    currentLengthUnit = settings.lengthUnit,
                                    onBack = { screen = Screen.Home },
                                    onExport = {
                                        scope.launch {
                                            withContext(Dispatchers.Default) { jsonDataManager.backup() } // Best-effort backup
                                            val json = withContext(Dispatchers.Default) { jsonDataManager.getRawJson() }
                                            val exportFileName = fileHandler.createTimestampedFileName("open-yarn-stash", "json")
                                            fileDownloader.download(exportFileName, json)
                                        }
                                    },
                                    onExportZip = {
                                         scope.launch {
                                            val exportFileName = fileHandler.createTimestampedFileName("files", "zip")
                                            fileDownloader.download(exportFileName, fileHandler.zipFiles())
                                        }
                                    },
                                    onImport = { fileContent ->
                                        scope.launch {
                                            try {
                                                withContext(Dispatchers.Default) {
                                                    jsonDataManager.importData(fileContent)
                                                }
                                                reloadAllData()
                                                snackbarHostState.showSnackbar("Import successful")
                                            } catch (e: Exception) {
                                                errorDialogMessage = "Failed to import data: ${e.message}. The data file might be corrupt."
                                            }
                                        }
                                    },
                                    onImportZip = { zipInputStream ->
                                        scope.launch {
                                            try {
                                                val timestamp = getCurrentTimestamp()
                                                fileHandler.renameFilesDirectory("files_$timestamp") // backup for debugging in case of error
                                                withContext(Dispatchers.Default) {
                                                    fileHandler.unzipAndReplaceFiles(zipInputStream)
                                                }
                                                reloadAllData()
                                                snackbarHostState.showSnackbar("ZIP import successful")
                                            } catch (e: Exception) {
                                                errorDialogMessage = "Failed to import ZIP: ${e.message}"
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
                                    },
                                    onLengthUnitChange = { newLengthUnit ->
                                        scope.launch {
                                            val newSettings = settings.copy(lengthUnit = newLengthUnit)
                                            withContext(Dispatchers.Default) {
                                                settingsManager.saveSettings(newSettings)
                                            }
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
