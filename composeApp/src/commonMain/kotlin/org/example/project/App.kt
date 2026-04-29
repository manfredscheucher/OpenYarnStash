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
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import openyarnstash.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.NoSuchElementException

@Composable
fun App(jsonDataManager: JsonDataManager, imageManager: ImageManager, fileDownloader: FileDownloader, fileHandler: FileHandler, settingsManager: JsonSettingsManager) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val pdfManager = remember { createPdfManager(fileHandler) }

    val appState = remember { AppState(jsonDataManager, imageManager, settingsManager, fileHandler, fileDownloader, pdfManager, scope) }

    val navStack by appState.navStack.collectAsState()
    val screen = navStack.last()
    val settings by appState.settings.collectAsState()
    val yarns by appState.yarns.collectAsState()
    val projects by appState.projects.collectAsState()
    val assignments by appState.assignments.collectAsState()
    val patterns by appState.patterns.collectAsState()
    val showNotImplementedDialog by appState.showNotImplementedDialog.collectAsState()
    val errorDialogMessage by appState.errorDialogMessage.collectAsState()
    val isExporting by appState.isExporting.collectAsState()
    val isImporting by appState.isImporting.collectAsState()
    val showExportSuccessDialog by appState.showExportSuccessDialog.collectAsState()
    val showImportSuccessDialog by appState.showImportSuccessDialog.collectAsState()
    val showDirtyBuildWarning by appState.showDirtyBuildWarning.collectAsState()
    val showFutureVersionWarning by appState.showFutureVersionWarning.collectAsState()
    val showAppExpiredWarning by appState.showAppExpiredWarning.collectAsState()
    val showRootedDeviceWarning by appState.showRootedDeviceWarning.collectAsState()

    LaunchedEffect(Unit) { appState.initialize() }

    LaunchedEffect(Unit) {
        appState.snackbarEvents.collect { snackbarHostState.showSnackbar(it) }
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
            is Screen.LicenseDetail -> "LicenseDetail(licenseType=${s.licenseType})"
        }
        Logger.log(LogLevel.INFO, "Navigating to screen: $screenName")
        Logger.logImportantFiles(LogLevel.TRACE)
    }

    if (showNotImplementedDialog) {
        AlertDialog(
            onDismissRequest = { appState.dismissNotImplementedDialog() },
            title = { Text(stringResource(Res.string.not_implemented_title)) },
            text = { Text(stringResource(Res.string.not_implemented_message)) },
            confirmButton = {
                TextButton(onClick = { appState.dismissNotImplementedDialog() }) {
                    Text(stringResource(Res.string.common_ok))
                }
            }
        )
    }

    if (errorDialogMessage != null) {
        AlertDialog(
            onDismissRequest = { appState.dismissErrorDialog() },
            title = { Text("Error") },
            text = { Text(errorDialogMessage!!) },
            confirmButton = {
                TextButton(onClick = { appState.dismissErrorDialog() }) {
                    Text(stringResource(Res.string.common_ok))
                }
            }
        )
    }

    if (showDirtyBuildWarning) {
        AlertDialog(
            onDismissRequest = { appState.dismissDirtyBuildWarning() },
            title = { Text(stringResource(Res.string.warning_dirty_build_title)) },
            text = { Text(stringResource(Res.string.warning_dirty_build_message)) },
            confirmButton = {
                TextButton(onClick = { appState.dismissDirtyBuildWarning() }) {
                    Text(stringResource(Res.string.common_ok))
                }
            }
        )
    }

    if (showFutureVersionWarning) {
        AlertDialog(
            onDismissRequest = { appState.dismissFutureVersionWarning() },
            title = { Text(stringResource(Res.string.warning_future_version_title)) },
            text = { Text(stringResource(Res.string.warning_future_version_message)) },
            confirmButton = {
                TextButton(onClick = { appState.dismissFutureVersionWarning() }) {
                    Text(stringResource(Res.string.common_ok))
                }
            }
        )
    }

    if (showRootedDeviceWarning) {
        AlertDialog(
            onDismissRequest = { /* not dismissable */ },
            title = { Text(stringResource(Res.string.warning_rooted_device_title)) },
            text = { Text(stringResource(Res.string.warning_rooted_device_message)) },
            confirmButton = {
                TextButton(onClick = { exitApp() }) {
                    Text(stringResource(Res.string.common_ok))
                }
            }
        )
    }

    if (showAppExpiredWarning) {
        AlertDialog(
            onDismissRequest = { /* not dismissable */ },
            title = { Text(stringResource(Res.string.warning_app_expired_title)) },
            text = { Text(stringResource(Res.string.warning_app_expired_message)) },
            confirmButton = {
                TextButton(onClick = { exitApp() }) {
                    Text(stringResource(Res.string.common_ok))
                }
            }
        )
    }

    key(currentSettings.language, currentSettings.lengthUnit, currentSettings.logLevel) {
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
                            onOpenYarns = { appState.navigateTo(Screen.YarnList) },
                            onOpenProjects = { appState.navigateTo(Screen.ProjectList) },
                            onOpenPatterns = { appState.navigateTo(Screen.PatternList) },
                            onOpenInfo = { appState.navigateTo(Screen.Info) },
                            onOpenStatistics = { appState.navigateTo(Screen.Statistics) },
                            onOpenSettings = { appState.navigateTo(Screen.Settings) },
                            onOpenHowToHelp = { appState.navigateTo(Screen.HowToHelp) }
                        )

                        Screen.YarnList -> {
                            val defaultYarnName = stringResource(Res.string.yarn_new_default_name)
                            YarnListScreen(
                                yarns = yarns.sortedByDescending { it.modified },
                                imageManager = imageManager,
                                assignments = assignments,
                                settings = currentSettings,
                                onAddClick = { appState.addYarn(defaultYarnName) },
                                onOpen = { id -> appState.navigateTo(Screen.YarnForm(id)) },
                                onBack = { appState.navigateBack() },
                                onSettingsChange = { newSettings -> appState.changeSettings(newSettings) }
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
                            var yarnImagesMap by remember { mutableStateOf<Map<UInt, ByteArray>>(emptyMap()) }

                            LaunchedEffect(s.yarnId, existingYarn) {
                                val imageMap = mutableMapOf<UInt, ByteArray>()
                                existingYarn?.imageIds?.forEach { imageId ->
                                    try {
                                        withContext(Dispatchers.Default) {
                                            imageManager.getYarnImage(existingYarn.id, imageId)
                                                ?.let {
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
                                LaunchedEffect(s.yarnId) { appState.navigateBack() }
                            } else {
                                val relatedUsages = assignments.filter { it.yarnId == existingYarn.id }
                                YarnFormScreen(
                                    initial = existingYarn,
                                    initialImages = yarnImagesMap,
                                    assignmentsForYarn = relatedUsages,
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
                                    onBack = { appState.navigateBack() },
                                    onDelete = { yarnIdToDelete -> appState.deleteYarn(yarnIdToDelete) },
                                    onSave = { editedYarn, newImages, callback ->
                                        appState.saveYarn(editedYarn, newImages, callback)
                                    },
                                    onAddColor = { yarnToCopy -> appState.addColorVariant(yarnToCopy) },
                                    onNavigateToProject = { projectId -> appState.navigateTo(Screen.ProjectForm(projectId)) }
                                )
                            }
                        }

                        Screen.ProjectList -> {
                            val defaultProjectName = stringResource(Res.string.project_new_default_name)
                            ProjectListScreen(
                                projects = projects.sortedByDescending { it.modified },
                                imageManager = imageManager,
                                settings = currentSettings,
                                onAddClick = { appState.addProject(defaultProjectName) },
                                onOpen = { id -> appState.navigateTo(Screen.ProjectForm(id)) },
                                onBack = { appState.navigateBack() },
                                onSettingsChange = { newSettings -> appState.changeSettings(newSettings) },
                                yarns = yarns,
                                assignments = assignments
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
                            var projectImagesMap by remember { mutableStateOf<Map<UInt, ByteArray>>(emptyMap()) }

                            LaunchedEffect(s.projectId, existingProject) {
                                val imageMap = mutableMapOf<UInt, ByteArray>()
                                existingProject?.imageIds?.forEach { imageId ->
                                    try {
                                        withContext(Dispatchers.Default) {
                                            imageManager.getProjectImage(existingProject.id, imageId)
                                                ?.let {
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
                                LaunchedEffect(s.projectId) { appState.navigateBack() }
                            } else {
                                val assignmentsForCurrentProject =
                                    assignments.filter { it.projectId == existingProject.id }
                                ProjectFormScreen(
                                    initial = existingProject,
                                    initialImages = projectImagesMap,
                                    assignmentsForProject = assignmentsForCurrentProject,
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
                                    onBack = { appState.navigateBack() },
                                    onDelete = { projectIdToDelete -> appState.deleteProject(projectIdToDelete) },
                                    onSave = { editedProject, newImages, callback ->
                                        appState.saveProject(editedProject, newImages, callback)
                                    },
                                    onNavigateToAssignments = {
                                        appState.navigateTo(Screen.ProjectAssignments(
                                            existingProject.id,
                                            existingProject.name
                                        ))
                                    },
                                    onNavigateToPattern = { patternId -> appState.navigateTo(Screen.PatternForm(patternId)) },
                                    onNavigateToYarn = { yarnId -> appState.navigateTo(Screen.YarnForm(yarnId)) }
                                )
                            }
                        }

                        is Screen.ProjectAssignments -> {
                            val initialAssignmentsForProject = assignments
                                .filter { it.projectId == s.projectId }
                                .associate { it.yarnId to it.amount }

                            ProjectAssignmentsScreen(
                                projectName = s.projectName,
                                allYarns = yarns,
                                initialAssignments = initialAssignmentsForProject,
                                getAvailableAmountForYarn = { yarnId ->
                                    try {
                                        jsonDataManager.availableForYarn(yarnId, forProjectId = s.projectId)
                                    } catch (e: NoSuchElementException) {
                                        scope.launch {
                                            Logger.log(LogLevel.ERROR, "Failed to get available amount for yarn $yarnId in ProjectAssignmentsScreen: ${e.message}", e)
                                        }
                                        0
                                    }
                                },
                                onSave = { updatedAssignments -> appState.setProjectAssignments(s.projectId, updatedAssignments) },
                                onBack = { appState.navigateBack() }
                            )
                        }

                        Screen.PatternList -> {
                            PatternListScreen(
                                patterns = patterns,
                                pdfManager = pdfManager,
                                onAddClick = { appState.addPattern() },
                                onOpen = { id -> appState.navigateTo(Screen.PatternForm(id)) },
                                onBack = { appState.navigateBack() }
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
                                LaunchedEffect(s.patternId) { appState.navigateBack() }
                            } else {
                                PatternFormScreen(
                                    initial = existingPattern,
                                    initialPdf = initialPdf,
                                    projects = projects,
                                    patterns = patterns,
                                    pdfManager = pdfManager,
                                    imageManager = imageManager,
                                    onBack = { appState.navigateBack() },
                                    onDelete = { patternIdToDelete -> appState.deletePattern(patternIdToDelete) },
                                    onSave = { editedPattern, pdf -> appState.savePattern(editedPattern, pdf, initialPdf) },
                                    onViewPdfExternally = { pdfManager.openPatternPdfExternally(s.patternId) },
                                    onNavigateToProject = { projectId -> appState.navigateTo(Screen.ProjectForm(projectId)) }
                                )
                            }
                        }

                        Screen.Info -> {
                            InfoScreen(
                                onBack = { appState.navigateBack() },
                                onNavigateToHelp = { appState.navigateTo(Screen.HowToHelp) },
                                onNavigateToLicense = { licenseType ->
                                    appState.navigateTo(Screen.LicenseDetail(licenseType))
                                }
                            )
                        }

                        is Screen.LicenseDetail -> {
                            LicenseDetailScreen(
                                licenseType = (screen as Screen.LicenseDetail).licenseType,
                                onBack = { appState.navigateBack() }
                            )
                        }

                        Screen.HowToHelp -> {
                            HowToHelpScreen(onBack = { appState.navigateBack() })
                        }

                        Screen.Statistics -> {
                            StatisticsScreen(
                                yarns = yarns,
                                projects = projects,
                                assignments = assignments,
                                onBack = { appState.navigateBack() },
                                settings = currentSettings
                            )
                        }

                        Screen.Settings -> {
                            SettingsScreen(
                                currentLocale = currentSettings.language,
                                currentLengthUnit = currentSettings.lengthUnit,
                                currentLogLevel = currentSettings.logLevel,
                                fileHandler = fileHandler,
                                onBack = { appState.navigateBack() },
                                onExportZip = { appState.exportZip() },
                                isExporting = isExporting,
                                isImporting = isImporting,
                                showExportSuccessDialog = showExportSuccessDialog,
                                showImportSuccessDialog = showImportSuccessDialog,
                                onDismissExportSuccess = { appState.dismissExportSuccessDialog() },
                                onDismissImportSuccess = { appState.dismissImportSuccessDialog() },
                                onImport = { fileContent -> appState.importJson(fileContent) },
                                onImportZip = { zipInputStream -> appState.importZip(zipInputStream) },
                                onLocaleChange = { newLocale -> appState.changeLocale(newLocale) },
                                onLengthUnitChange = { newLengthUnit -> appState.changeLengthUnit(newLengthUnit) },
                                onLogLevelChange = { newLogLevel -> appState.changeLogLevel(newLogLevel) }
                            )
                        }
                    }
                }
            }
        }
    }
}
