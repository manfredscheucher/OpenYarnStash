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
    data class PdfViewer(val patternId: Int) : Screen()
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
    val logger = remember(settings) { Logger(fileHandler, settings) }

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
            logger.log(LogLevel.ERROR, "Failed to load data in fun reloadAllData")
            logger.log(LogLevel.DEBUG, "Error details: $e")
        }
        logger.log(LogLevel.INFO,"Data reloaded" )
        logger.logImportantFiles(LogLevel.TRACE)
    }

    LaunchedEffect(Unit) {
        settings = withContext(Dispatchers.Default) { settingsManager.loadSettings() }
    }

    LaunchedEffect(settings) {
        logger.log(LogLevel.INFO,"Settings reloaded" )
        logger.logImportantFiles(LogLevel.DEBUG)
        setAppLanguage(settings.language)
        reloadAllData()
    }

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
            is Screen.PdfViewer -> "PdfViewer(patternId=${s.patternId})"
        }
        logger.log(LogLevel.INFO, "Navigating to screen: $screenName")
        logger.logImportantFiles(LogLevel.TRACE)
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
            key(settings.language, settings.lengthUnit, settings.logLevel) {
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
                                    scope.launch {
                                        logger.log(LogLevel.ERROR, "Failed to get yarn by id ${s.yarnId} in YarnForm")
                                        logger.log(LogLevel.DEBUG, "Error details: $e")
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
                                                    logger.log(LogLevel.WARN, "Image not found for yarn ${existingYarn.id}, imageId $imageId")
                                                }
                                        }
                                    } catch (e: Exception) {
                                        scope.launch {
                                            logger.log(LogLevel.ERROR, "Failed to load image for yarn ${existingYarn.id}, imageId $imageId")
                                            logger.log(LogLevel.DEBUG, "Error details: $e")
                                        }
                                    }
                                }
                                yarnImagesMap = imageMap
                            }

                            if (existingYarn == null) {
                                LaunchedEffect(s.yarnId) { screen = Screen.YarnList }
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
                                                    logger.log(LogLevel.WARN, "Project with id $pid not found, referenced by yarn ${existingYarn.id}")
                                                }
                                            }
                                        }
                                    },
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
                                            val finalImageIds = editedYarn.imageIds
                                            val idsToDelete = existingImageIds.filter { it !in finalImageIds }

                                            withContext(Dispatchers.Default) {
                                                idsToDelete.forEach { imageId ->
                                                    imageManager.deleteYarnImage(editedYarn.id, imageId)
                                                }
                                                newImages.entries.forEach { (imageId, imageData) ->
                                                    imageManager.saveYarnImage(
                                                        editedYarn.id,
                                                        imageId,
                                                        imageData
                                                    )
                                                }
                                                jsonDataManager.addOrUpdateYarn(editedYarn)
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
                                    onNavigateToProject = { projectId -> screen = Screen.ProjectForm(projectId) }
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
                                    scope.launch {
                                        logger.log(LogLevel.ERROR, "Failed to get project by id ${s.projectId} in ProjectForm")
                                        logger.log(LogLevel.DEBUG, "Error details: $e")
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
                                                logger.log(LogLevel.WARN, "Image not found for project ${existingProject.id}, imageId $imageId")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        scope.launch {
                                            logger.log(LogLevel.ERROR, "Failed to load image for project ${existingProject.id}, imageId $imageId")
                                            logger.log(LogLevel.DEBUG, "Error details: $e")
                                        }
                                    }
                                }
                                projectImagesMap = imageMap
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
                                    yarnById = { yarnId ->
                                        yarns.firstOrNull { it.id == yarnId }.also {
                                            if (it == null) {
                                                scope.launch {
                                                    logger.log(LogLevel.WARN, "Yarn with id $yarnId not found, referenced by project ${existingProject.id}")
                                                }
                                            }
                                        }
                                    },
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
                                            val finalImageIds = editedProject.imageIds
                                            val idsToDelete = existingImageIds.filter { it !in finalImageIds }
                                            
                                            withContext(Dispatchers.Default) {
                                                idsToDelete.forEach { imageId ->
                                                    imageManager.deleteProjectImage(editedProject.id, imageId)
                                                }
                                                newImages.entries.sortedBy { it.key }.forEach { (imageId, imageData) ->
                                                    imageManager.saveProjectImage(
                                                        editedProject.id,
                                                        imageId,
                                                        imageData
                                                    )
                                                }
                                                jsonDataManager.addOrUpdateProject(editedProject)
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
                                    },
                                    onNavigateToYarn = { yarnId ->
                                        screen = Screen.YarnForm(yarnId)
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
                                            logger.log(LogLevel.ERROR, "Failed to get available amount for yarn $yarnId in ProjectAssignmentsScreen")
                                            logger.log(LogLevel.DEBUG, "Error details: $e")
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
                                        screen = Screen.ProjectForm(s.projectId)
                                    }
                                },
                                onBack = { screen = Screen.ProjectForm(s.projectId) }
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
                                    scope.launch {
                                        logger.log(LogLevel.ERROR, "Failed to get pattern by id ${s.patternId} in PatternForm")
                                        logger.log(LogLevel.DEBUG, "Error details: $e")
                                    }
                                    null
                                }
                            }

                            var initialPdf by remember { mutableStateOf<ByteArray?>(null) }

                            LaunchedEffect(s.patternId, existingPattern) {
                                try {
                                    withContext(Dispatchers.Default) {
                                        pdfManager.getPatternPdf(existingPattern!!.id)
                                            ?. let{
                                                initialPdf = it
                                            } ?: scope.launch {
                                            logger.log(LogLevel.WARN, "PDF not found for pattern ${existingPattern.id}")
                                        }
                                    }
                                } catch (e: Exception) {
                                    scope.launch {
                                        logger.log(LogLevel.ERROR, "Failed to load PDF for pattern ${existingPattern?.id}")
                                        logger.log(LogLevel.DEBUG, "Error details: $e")
                                    }
                                    initialPdf = null
                                }
                            }

                            if (existingPattern == null) {
                                LaunchedEffect(s.patternId) { screen = Screen.PatternList }
                            } else {
                                PatternFormScreen(
                                    initial = existingPattern,
                                    initialPdf = initialPdf,
                                    projects = projects,
                                    patterns = patterns,
                                    pdfManager = pdfManager,
                                    imageManager = imageManager,
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
                                    },
                                    onViewPdf = { screen = Screen.PdfViewer(s.patternId) },
                                    onViewPdfExternally = { pdfManager.openPatternPdfExternally(s.patternId) },
                                    onNavigateToProject = { projectId -> screen = Screen.ProjectForm(projectId) }
                                )
                            }
                        }

                        is Screen.PdfViewer -> {
                            var pdf by remember { mutableStateOf<ByteArray?>(null) }

                            LaunchedEffect(s.patternId) {
                                pdf = withContext(Dispatchers.Default) {
                                    pdfManager.getPatternPdf(s.patternId)
                                }
                            }

                            if (pdf != null) {
                                PdfViewerScreen(
                                    pdf = pdf!!,
                                    onBack = { screen = Screen.PatternForm(s.patternId) }
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
                                    currentLogLevel = settings.logLevel,
                                    onBack = { screen = Screen.Home },
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
                                                val errorMessage = "Failed to import data: ${e.message}. The data file might be corrupt."
                                                errorDialogMessage = errorMessage
                                                scope.launch {
                                                    logger.log(LogLevel.ERROR, "Failed to import data in onImport")
                                                    logger.log(LogLevel.DEBUG, "Error details: $e")
                                                }
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
                                                val errorMessage = "Failed to import ZIP: ${e.message}"
                                                errorDialogMessage = errorMessage
                                                scope.launch {
                                                    logger.log(LogLevel.ERROR, "Failed to import ZIP in onImportZip")
                                                    logger.log(LogLevel.DEBUG, "Error details: $e")
                                                }
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
                                    },
                                    onLogLevelChange = { newLogLevel ->
                                        scope.launch {
                                            val newSettings = settings.copy(logLevel = newLogLevel)
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
