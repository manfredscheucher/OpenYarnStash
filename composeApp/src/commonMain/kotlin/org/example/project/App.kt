package org.example.project

import androidx.compose.foundation.layout.Box
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
    data class YarnForm(val yarnId: Int) : Screen()
    data object ProjectList : Screen()
    data class ProjectForm(val projectId: Int) : Screen()
    data class ProjectAssignments(val projectId: Int, val projectName: String) : Screen()
    data object Info : Screen()
    data object HowToHelp : Screen()
    data object Statistics : Screen()
    data object Settings : Screen()
    data object PatternList : Screen()
    data class PatternForm(val patternId: Int) : Screen()
}

@Composable
fun App(jsonDataManager: JsonDataManager, imageManager: ImageManager, fileDownloader: FileDownloader, fileHandler: FileHandler, settingsManager: JsonSettingsManager) {
    var navStack by remember { mutableStateOf(listOf<Screen>(Screen.Home)) }
    val screen = navStack.last()
    var settings by remember { mutableStateOf<Settings?>(null) }
    var yarns by remember { mutableStateOf(emptyList<Yarn>()) }
    var projects by remember { mutableStateOf(emptyList<Project>()) }
    var usages by remember { mutableStateOf(emptyList<Usage>()) }
    var patterns by remember { mutableStateOf(emptyList<Pattern>()) }
    var showNotImplementedDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val pdfManager = createPdfManager(fileHandler)

    fun navigateTo(newScreen: Screen) {
        navStack = navStack + newScreen
    }

    fun navigateBack() {
        if (navStack.size > 1) {
            navStack = navStack.dropLast(1)
        }
    }

    suspend fun reloadAllData() {
        try {
            val data = withContext(Dispatchers.Default) { jsonDataManager.load() }
            yarns = data.yarns
            projects = data.projects
            usages = data.usages
            patterns = data.patterns
        } catch (e: Exception) {
            val errorMessage = "Failed to load data: ${e.message}. The data file might be corrupt."
            errorDialogMessage = errorMessage
            Logger.log(LogLevel.ERROR, "Failed to load data in fun reloadAllData: ${e.message}", e)
        }
        Logger.log(LogLevel.INFO,"Data reloaded" )
        Logger.logImportantFiles(LogLevel.TRACE)
    }

    LaunchedEffect(Unit) {
        val loadedSettings = withContext(Dispatchers.Default) { settingsManager.loadSettings() }
        setAppLanguage(loadedSettings.language)
        settings = loadedSettings
        reloadAllData()
    }

    LaunchedEffect(settings) {
        settings?.let {
            Logger.log(LogLevel.INFO,"Settings reloaded" )
            Logger.logImportantFiles(LogLevel.DEBUG)
        }
    }

    // Don't render UI until settings are loaded
    val currentSettings = settings ?: return

    LaunchedEffect(screen) {
        val screenName = when (val s = screen) {
            is Screen.Home -> "Home"
            is Screen.YarnList -> "YarnList"
            is Screen.YarnForm -> "YarnForm(yarnId=${s.yarnId})"
            is Screen.ProjectList -> "ProjectList"
            is Screen.ProjectForm -> "ProjectForm(projectId=${s.projectId})"
            is Screen.ProjectAssignments -> "ProjectAssignments(projectId=${s.projectId}, projectName='${s.projectName}')"
            is Screen.Info -> "Info"
            is Screen.HowToHelp -> "HowToHelp"
            is Screen.Statistics -> "Statistics"
            is Screen.Settings -> "Settings"
            is Screen.PatternList -> "PatternList"
            is Screen.PatternForm -> "PatternForm(patternId=${s.patternId})"
        }
        Logger.log(LogLevel.INFO, "Navigating to screen: $screenName")
        Logger.logImportantFiles(LogLevel.TRACE)
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

    key(currentSettings.language, currentSettings.lengthUnit, currentSettings.logLevel, currentSettings.backupOldFolderOnImport) {
        MaterialTheme(
            colorScheme = LightColorScheme
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                ) {
                    when (val s = screen) {
                        Screen.Home -> HomeScreen(
                            onOpenYarns = { navigateTo(Screen.YarnList) },
                            onOpenProjects = { navigateTo(Screen.ProjectList) },
                            onOpenPatterns = { navigateTo(Screen.PatternList) },
                            onOpenInfo = { navigateTo(Screen.Info) },
                            onOpenStatistics = { navigateTo(Screen.Statistics) },
                            onOpenSettings = { navigateTo(Screen.Settings) },
                            onOpenHowToHelp = { navigateTo(Screen.HowToHelp) }
                        )

                        Screen.YarnList -> {
                            val defaultYarnName = stringResource(Res.string.yarn_new_default_name)
                            YarnListScreen(
                                yarns = yarns.sortedByDescending { it.modified },
                                imageManager = imageManager,
                                usages = usages,
                                settings = currentSettings,
                                onAddClick = {
                                    scope.launch {
                                        val newYarn = jsonDataManager.createNewYarn(defaultYarnName)
                                        withContext(Dispatchers.Default) { jsonDataManager.addOrUpdateYarn(newYarn) }
                                        reloadAllData()
                                        navigateTo(Screen.YarnForm(newYarn.id))
                                    }
                                },
                                onOpen = { id -> navigateTo(Screen.YarnForm(id)) },
                                onBack = { navigateBack() },
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
                                    scope.launch {
                                        Logger.log(LogLevel.ERROR, "Failed to get yarn by id ${s.yarnId} in YarnForm: ${e.message}", e)
                                    }
                                    null
                                }
                            }
                            var yarnImagesMap by remember { mutableStateOf<Map<Int, ByteArray>>(emptyMap()) }

                            LaunchedEffect(s.yarnId, existingYarn) {
                                val imageMap = mutableMapOf<Int, ByteArray>()
                                existingYarn?.imageIds?.forEach { imageId ->
                                    try {
                                        withContext(Dispatchers.Default) {
                                            imageManager.getYarnImage(existingYarn.id, imageId)
                                                ?. let{
                                                    imageMap[imageId] = it
                                                } ?: scope.launch {
                                                Logger.log(LogLevel.WARN, "Image not found for yarn ${existingYarn.id}, imageId $imageId")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        scope.launch {
                                            Logger.log(LogLevel.ERROR, "Failed to load image for yarn ${existingYarn.id}, imageId $imageId: ${e.message}", e)
                                        }
                                    }
                                }
                                yarnImagesMap = imageMap
                            }

                            if (existingYarn == null) {
                                LaunchedEffect(s.yarnId) { navigateBack() }
                            } else {
                                val relatedUsages = usages.filter { it.yarnId == existingYarn.id }
                                YarnFormScreen(
                                    initial = existingYarn,
                                    initialImages = yarnImagesMap,
                                    usagesForYarn = relatedUsages,
                                    projectById = { pid ->
                                        projects.firstOrNull { it.id == pid }.also {
                                            if (it == null) {
                                                scope.launch {
                                                    Logger.log(LogLevel.WARN, "Project with id $pid not found, referenced by yarn ${existingYarn.id}")
                                                }
                                            }
                                        }
                                    },
                                    imageManager = imageManager,
                                    settings = currentSettings,
                                    onBack = { navigateBack() },
                                    onDelete = { yarnIdToDelete ->
                                        scope.launch {
                                            try {
                                                withContext(Dispatchers.Default) {
                                                    val yarnToDelete = jsonDataManager.getYarnById(yarnIdToDelete)
                                                    yarnToDelete!!.imageIds.forEach { imageId ->
                                                        imageManager.deleteYarnImage(yarnIdToDelete, imageId)
                                                    }
                                                    jsonDataManager.deleteYarn(yarnIdToDelete)
                                                }
                                                reloadAllData()
                                                navStack = navStack.filterNot { it is Screen.YarnForm && it.yarnId == yarnIdToDelete }
                                            } catch (e: Exception) {
                                                Logger.log(LogLevel.ERROR, "Failed to delete yarn with id $yarnIdToDelete: ${e.message}", e)
                                                errorDialogMessage = "Failed to delete yarn: ${e.message}"
                                            }
                                        }
                                    },
                                    onSave = { editedYarn, newImages ->
                                        scope.launch {
                                            val existingImageIds = existingYarn.imageIds
                                            val newImagesToUpload = newImages.filter { it.key !in existingYarn.imageIds }
                                            val idsToDelete = existingImageIds.filter { it !in newImages.keys }
                                            Logger.log(LogLevel.DEBUG, "Upload images: $newImagesToUpload")
                                            Logger.log(LogLevel.DEBUG, "Removing old images with ids: $idsToDelete")

                                            withContext(Dispatchers.Default) {
                                                idsToDelete.forEach { imageId ->
                                                    imageManager.deleteYarnImage(editedYarn.id, imageId)
                                                }

                                                newImagesToUpload.entries.forEach { (imageId, imageData) ->
                                                    imageManager.saveYarnImage(
                                                        editedYarn.id,
                                                        imageId,
                                                        imageData
                                                    )
                                                }

                                                jsonDataManager.addOrUpdateYarn(editedYarn)
                                            }
                                            reloadAllData()
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
                                            navigateTo(Screen.YarnForm(newYarn.id))
                                        }
                                    },
                                    onNavigateToProject = { projectId -> navigateTo(Screen.ProjectForm(projectId)) }
                                )
                            }
                        }

                        Screen.ProjectList -> {
                            val defaultProjectName =
                                stringResource(Res.string.project_new_default_name)
                            ProjectListScreen(
                                projects = projects.sortedByDescending { it.modified },
                                imageManager = imageManager,
                                settings = currentSettings,
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
                                        navigateTo(Screen.ProjectForm(newProject.id))
                                    }
                                },
                                onOpen = { id -> navigateTo(Screen.ProjectForm(id)) },
                                onBack = { navigateBack() },
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
                                    scope.launch {
                                        Logger.log(LogLevel.ERROR, "Failed to get project by id ${s.projectId} in ProjectForm: ${e.message}", e)
                                    }
                                    null
                                }
                            }
                            var projectImagesMap by remember { mutableStateOf<Map<Int, ByteArray>>(emptyMap()) }

                            LaunchedEffect(s.projectId, existingProject) {
                                val imageMap = mutableMapOf<Int, ByteArray>()
                                existingProject?.imageIds?.forEach { imageId ->
                                    try {
                                        withContext(Dispatchers.Default) {
                                            imageManager.getProjectImage(existingProject.id, imageId)
                                                ?. let{
                                                    imageMap[imageId] = it
                                                } ?: scope.launch {
                                                Logger.log(LogLevel.WARN, "Image not found for project ${existingProject.id}, imageId $imageId")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        scope.launch {
                                            Logger.log(LogLevel.ERROR, "Failed to load image for project ${existingProject.id}, imageId $imageId: ${e.message}", e)
                                        }
                                    }
                                }
                                projectImagesMap = imageMap
                            }

                            if (existingProject == null) {
                                LaunchedEffect(s.projectId) { navigateBack() }
                            } else {
                                val usagesForCurrentProject =
                                    usages.filter { it.projectId == existingProject.id }
                                ProjectFormScreen(
                                    initial = existingProject,
                                    initialImages = projectImagesMap,
                                    usagesForProject = usagesForCurrentProject,
                                    yarnById = { yarnId ->
                                        yarns.firstOrNull { it.id == yarnId }.also {
                                            if (it == null) {
                                                scope.launch {
                                                    Logger.log(LogLevel.WARN, "Yarn with id $yarnId not found, referenced by project ${existingProject.id}")
                                                }
                                            }
                                        }
                                    },
                                    patterns = patterns,
                                    imageManager = imageManager,
                                    onBack = { navigateBack() },
                                    onDelete = { projectIdToDelete ->
                                        scope.launch {
                                            try {
                                                withContext(Dispatchers.Default) {
                                                    val projectToDelete = jsonDataManager.getProjectById(projectIdToDelete)
                                                    projectToDelete!!.imageIds.forEach { imageId ->
                                                        imageManager.deleteProjectImage(projectIdToDelete, imageId)
                                                    }
                                                    jsonDataManager.deleteProject(projectIdToDelete)
                                                }
                                                reloadAllData()
                                                navStack = navStack.filterNot { (it is Screen.ProjectForm && it.projectId == projectIdToDelete) || (it is Screen.ProjectAssignments && it.projectId == projectIdToDelete) }
                                            } catch (e: Exception) {
                                                Logger.log(LogLevel.ERROR, "Failed to delete project with id $projectIdToDelete: ${e.message}", e)
                                                errorDialogMessage = "Failed to delete project: ${e.message}"
                                            }
                                        }
                                    },
                                    onSave = { editedProject, newImages ->
                                        scope.launch {
                                            val existingImageIds = existingProject.imageIds
                                            val newImagesToUpload = newImages.filter { it.key !in existingImageIds }
                                            val idsToDelete = existingImageIds.filter { it !in newImages.keys }
                                            Logger.log(LogLevel.DEBUG, "Upload images: $newImagesToUpload")
                                            Logger.log(LogLevel.DEBUG, "Removing old images with ids: $idsToDelete")

                                            withContext(Dispatchers.Default) {
                                                idsToDelete.forEach { imageId ->
                                                    imageManager.deleteProjectImage(editedProject.id, imageId)
                                                }
                                                newImagesToUpload.entries.sortedBy { it.key }.forEach { (imageId, imageData) ->
                                                    imageManager.saveProjectImage(
                                                        editedProject.id,
                                                        imageId,
                                                        imageData
                                                    )
                                                }

                                                jsonDataManager.addOrUpdateProject(editedProject)
                                            }
                                            reloadAllData()
                                        }
                                    },
                                    onNavigateToAssignments = {
                                        navigateTo(Screen.ProjectAssignments(
                                            existingProject.id,
                                            existingProject.name
                                        ))
                                    },
                                    onNavigateToPattern = { patternId ->
                                        navigateTo(Screen.PatternForm(patternId))
                                    },
                                    onNavigateToYarn = { yarnId ->
                                        navigateTo(Screen.YarnForm(yarnId))
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
                                        scope.launch {
                                            Logger.log(LogLevel.ERROR, "Failed to get available amount for yarn $yarnId in ProjectAssignmentsScreen: ${e.message}", e)
                                        }
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
                                    }
                                },
                                onBack = { navigateBack() }
                            )
                        }

                        Screen.PatternList -> {
                            PatternListScreen(
                                patterns = patterns,
                                pdfManager = pdfManager,
                                onAddClick = {
                                    scope.launch {
                                        val newPattern = jsonDataManager.createNewPattern()
                                        withContext(Dispatchers.Default) { jsonDataManager.addOrUpdatePattern(newPattern) }
                                        reloadAllData()
                                        navigateTo(Screen.PatternForm(newPattern.id))
                                    }
                                },
                                onOpen = { id -> navigateTo(Screen.PatternForm(id)) },
                                onBack = { navigateBack() }
                            )
                        }

                        is Screen.PatternForm -> {
                            val existingPattern = remember(s.patternId, patterns) {
                                try {
                                    jsonDataManager.getPatternById(s.patternId)
                                } catch (e: NoSuchElementException) {
                                    scope.launch {
                                        Logger.log(LogLevel.ERROR, "Failed to get pattern by id ${s.patternId} in PatternForm: ${e.message}", e)
                                    }
                                    null
                                }
                            }

                            var initialPdf by remember { mutableStateOf<ByteArray?>(null) }

                            LaunchedEffect(s.patternId, existingPattern) {
                                if (existingPattern != null) {
                                    try {
                                        withContext(Dispatchers.Default) {
                                            pdfManager.getPatternPdf(existingPattern.id)
                                                ?.let { initialPdf = it }
                                                ?: scope.launch {
                                                    Logger.log(LogLevel.WARN, "PDF not found for pattern ${existingPattern.id}")
                                                }
                                        }
                                    } catch (e: Exception) {
                                        scope.launch {
                                            Logger.log(LogLevel.ERROR, "Failed to load PDF for pattern ${existingPattern.id}: ${e.message}", e)
                                        }
                                        initialPdf = null
                                    }
                                }
                            }

                            if (existingPattern == null) {
                                LaunchedEffect(s.patternId) { navigateBack() }
                            } else {
                                PatternFormScreen(
                                    initial = existingPattern,
                                    initialPdf = initialPdf,
                                    projects = projects,
                                    patterns = patterns,
                                    pdfManager = pdfManager,
                                    imageManager = imageManager,
                                    onBack = { navigateBack() },
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
                                            navStack = navStack.filterNot { it is Screen.PatternForm && it.patternId == patternIdToDelete }
                                        }
                                    },
                                    onSave = { editedPattern, pdf ->
                                        scope.launch {
                                            withContext(Dispatchers.Default) {
                                                if (initialPdf == null && pdf != null || initialPdf != null && pdf == null || (initialPdf != null && pdf != null && !initialPdf.contentEquals(pdf))) {
                                                    if (pdf != null) {
                                                        pdfManager.savePatternPdf(editedPattern.id, pdf)
                                                    } else {
                                                        pdfManager.deletePatternPdf(editedPattern.id)
                                                    }
                                                }
                                                jsonDataManager.addOrUpdatePattern(editedPattern)
                                            }
                                            reloadAllData()
                                        }
                                    },
                                    onViewPdfExternally = { pdfManager.openPatternPdfExternally(s.patternId) },
                                    onNavigateToProject = { projectId -> navigateTo(Screen.ProjectForm(projectId)) }
                                )
                            }
                        }

                        Screen.Info -> {
                            InfoScreen(onBack = { navigateBack() }, onNavigateToHelp = { navigateTo(Screen.HowToHelp) })
                        }

                        Screen.HowToHelp -> {
                            HowToHelpScreen(onBack = { navigateBack() })
                        }

                        Screen.Statistics -> {
                            StatisticsScreen(
                                yarns = yarns,
                                projects = projects,
                                usages = usages,
                                onBack = { navigateBack() },
                                settings = currentSettings
                            )
                        }

                        Screen.Settings -> {
                            SettingsScreen(
                                currentLocale = currentSettings.language,
                                currentLengthUnit = currentSettings.lengthUnit,
                                currentLogLevel = currentSettings.logLevel,
                                backupOldFolderOnImport = currentSettings.backupOldFolderOnImport,
                                fileHandler = fileHandler,
                                onBack = { navigateBack() },
                                onExportZip = {
                                    scope.launch {
                                        val exportFileName = fileHandler.createTimestampedFileName("files", "zip")
                                        fileDownloader.download(exportFileName, fileHandler.zipFiles(), getContext())
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
                                            val errorMessage = "Failed to import data: ${e.message}. The data file might be corrupt."
                                            errorDialogMessage = errorMessage
                                            scope.launch {
                                                Logger.log(LogLevel.ERROR, "Failed to import data in onImport: ${e.message}", e)
                                            }
                                        }
                                    }
                                },
                                onImportZip = { zipInputStream ->
                                    scope.launch {
                                        try {
                                            if (currentSettings.backupOldFolderOnImport) {
                                                val timestamp = getCurrentTimestamp()
                                                fileHandler.renameFilesDirectory("files_$timestamp") // backup for debugging in case of error
                                            } else {
                                                fileHandler.deleteFilesDirectory()
                                            }
                                            withContext(Dispatchers.Default) {
                                                fileHandler.unzipAndReplaceFiles(zipInputStream)
                                            }
                                            reloadAllData()
                                            snackbarHostState.showSnackbar("ZIP import successful")
                                        } catch (e: Exception) {
                                            val errorMessage = "Failed to import ZIP: ${e.message}"
                                            errorDialogMessage = errorMessage
                                            scope.launch {
                                                Logger.log(LogLevel.ERROR, "Failed to import ZIP in onImportZip: ${e.message}", e)
                                            }
                                        }
                                    }
                                },
                                onLocaleChange = { newLocale ->
                                    scope.launch {
                                        val newSettings = currentSettings.copy(language = newLocale)
                                        withContext(Dispatchers.Default) {
                                            settingsManager.saveSettings(newSettings)
                                        }
                                        setAppLanguage(newLocale)
                                        settings = newSettings
                                    }
                                },
                                onLengthUnitChange = { newLengthUnit ->
                                    scope.launch {
                                        val newSettings = currentSettings.copy(lengthUnit = newLengthUnit)
                                        withContext(Dispatchers.Default) {
                                            settingsManager.saveSettings(newSettings)
                                        }
                                        settings = newSettings
                                    }
                                },
                                onLogLevelChange = { newLogLevel ->
                                    scope.launch {
                                        val newSettings = currentSettings.copy(logLevel = newLogLevel)
                                        withContext(Dispatchers.Default) {
                                            settingsManager.saveSettings(newSettings)
                                        }
                                        settings = newSettings
                                    }
                                },
                                onBackupOldFolderOnImportChange = { newBackupOldFolderOnImport ->
                                    scope.launch {
                                        val newSettings = currentSettings.copy(backupOldFolderOnImport = newBackupOldFolderOnImport)
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
