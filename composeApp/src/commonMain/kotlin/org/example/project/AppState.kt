package org.example.project

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

sealed class Screen {
    data object Home : Screen()
    data object YarnList : Screen()
    data class YarnForm(val yarnId: UInt) : Screen()
    data object ProjectList : Screen()
    data class ProjectForm(val projectId: UInt) : Screen()
    data class ProjectAssignments(val projectId: UInt, val projectName: String) : Screen()
    data object Info : Screen()
    data object HowToHelp : Screen()
    data object Statistics : Screen()
    data object Settings : Screen()
    data object PatternList : Screen()
    data class PatternForm(val patternId: UInt) : Screen()
    data class LicenseDetail(val licenseType: LicenseType) : Screen()
}

class AppState(
    private val jsonDataManager: JsonDataManager,
    private val imageManager: ImageManager,
    private val settingsManager: JsonSettingsManager,
    private val fileHandler: FileHandler,
    private val fileDownloader: FileDownloader,
    private val pdfManager: PdfManager,
    val scope: CoroutineScope
) {
    private val _navStack = MutableStateFlow<List<Screen>>(listOf(Screen.Home))
    val navStack: StateFlow<List<Screen>> = _navStack.asStateFlow()
    val currentScreen get() = _navStack.value.last()

    private val _settings = MutableStateFlow<Settings?>(null)
    val settings: StateFlow<Settings?> = _settings.asStateFlow()

    private val _yarns = MutableStateFlow<List<Yarn>>(emptyList())
    val yarns: StateFlow<List<Yarn>> = _yarns.asStateFlow()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _assignments = MutableStateFlow<List<Assignment>>(emptyList())
    val assignments: StateFlow<List<Assignment>> = _assignments.asStateFlow()

    private val _patterns = MutableStateFlow<List<Pattern>>(emptyList())
    val patterns: StateFlow<List<Pattern>> = _patterns.asStateFlow()

    private val _showNotImplementedDialog = MutableStateFlow(false)
    val showNotImplementedDialog: StateFlow<Boolean> = _showNotImplementedDialog.asStateFlow()

    private val _errorDialogMessage = MutableStateFlow<String?>(null)
    val errorDialogMessage: StateFlow<String?> = _errorDialogMessage.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _showExportSuccessDialog = MutableStateFlow(false)
    val showExportSuccessDialog: StateFlow<Boolean> = _showExportSuccessDialog.asStateFlow()

    private val _showImportSuccessDialog = MutableStateFlow(false)
    val showImportSuccessDialog: StateFlow<Boolean> = _showImportSuccessDialog.asStateFlow()

    private val _showDirtyBuildWarning = MutableStateFlow(false)
    val showDirtyBuildWarning: StateFlow<Boolean> = _showDirtyBuildWarning.asStateFlow()

    private val _showFutureVersionWarning = MutableStateFlow(false)
    val showFutureVersionWarning: StateFlow<Boolean> = _showFutureVersionWarning.asStateFlow()

    private val _showAppExpiredWarning = MutableStateFlow(false)
    val showAppExpiredWarning: StateFlow<Boolean> = _showAppExpiredWarning.asStateFlow()

    private val _showRootedDeviceWarning = MutableStateFlow(false)
    val showRootedDeviceWarning: StateFlow<Boolean> = _showRootedDeviceWarning.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    // --- Navigation ---

    fun navigateTo(screen: Screen) {
        _navStack.value = _navStack.value + screen
    }

    fun navigateBack() {
        if (_navStack.value.size > 1) {
            _navStack.value = _navStack.value.dropLast(1)
        }
    }

    fun removeScreensFromStack(predicate: (Screen) -> Boolean) {
        _navStack.value = _navStack.value.filterNot(predicate)
    }

    // --- Data Loading ---

    suspend fun reloadAllData() {
        try {
            val data = withContext(Dispatchers.Default) { jsonDataManager.load() }
            _yarns.value = data.yarns
            _projects.value = data.projects
            _assignments.value = data.assignments
            _patterns.value = data.patterns
        } catch (e: Exception) {
            val errorMessage = "Failed to load data: ${e.message}. The data file might be corrupt."
            _errorDialogMessage.value = errorMessage
            Logger.log(LogLevel.ERROR, "Failed to load data in reloadAllData: ${e.message}", e)
            throw e
        }
        Logger.log(LogLevel.INFO, "Data reloaded")
        Logger.logImportantFiles(LogLevel.TRACE)
    }

    // --- Initialization ---

    @OptIn(ExperimentalTime::class)
    suspend fun initialize() {
        val loadedSettings = withContext(Dispatchers.Default) { settingsManager.loadSettings() }
        setAppLanguage(loadedSettings.language)
        _settings.value = loadedSettings
        Logger.updateSettings(loadedSettings)
        reloadAllData()

        withContext(Dispatchers.Default) { settingsManager.saveSettings(loadedSettings) }
        _settings.value = settingsManager.settings

        if (isDeviceRooted()) {
            _showRootedDeviceWarning.value = true
            return
        }

        val expirationDays = GeneratedVersionInfo.EXPIRATION_DAYS
        if (expirationDays > 0) {
            try {
                val buildInstant = Instant.parse(GeneratedVersionInfo.BUILD_DATE)
                val nowInstantVal = nowInstant()
                val expirationSeconds = expirationDays.toLong() * 24 * 60 * 60
                if ((nowInstantVal - buildInstant).inWholeSeconds > expirationSeconds) {
                    _showAppExpiredWarning.value = true
                    return
                }
            } catch (_: Exception) { }
        }

        if (GeneratedVersionInfo.IS_DIRTY == "dirty") {
            _showDirtyBuildWarning.value = true
        }
        val savedVersionInfo = loadedSettings.versionInfo
        if (savedVersionInfo.commitDate.isNotEmpty()) {
            try {
                val savedBuildInstant = Instant.parse(savedVersionInfo.commitDate)
                val currentBuildInstant = Instant.parse(GeneratedVersionInfo.COMMIT_DATE)
                if (savedBuildInstant > currentBuildInstant) {
                    _showFutureVersionWarning.value = true
                }
            } catch (_: Exception) { }
        }
    }

    // --- Yarn operations ---

    fun addYarn(defaultName: String) {
        scope.launch {
            val newYarn = jsonDataManager.createNewYarn(defaultName)
            withContext(Dispatchers.Default) { jsonDataManager.addOrUpdateYarn(newYarn) }
            reloadAllData()
            navigateTo(Screen.YarnForm(newYarn.id))
        }
    }

    fun saveYarn(editedYarn: Yarn, newImages: Map<UInt, ByteArray>, onDone: (() -> Unit)? = null) {
        scope.launch {
            val existingYarn = try {
                jsonDataManager.getYarnById(editedYarn.id)
            } catch (e: Exception) {
                Logger.log(LogLevel.ERROR, "Failed to get yarn ${editedYarn.id} for save: ${e.message}", e)
                null
            }
            val existingImageIds = existingYarn?.imageIds ?: emptyList()
            val newImagesToUpload = newImages.filter { it.key !in existingImageIds }
            val idsToDelete = existingImageIds.filter { it !in newImages.keys }

            withContext(Dispatchers.Default) {
                idsToDelete.forEach { imageId ->
                    imageManager.deleteYarnImage(editedYarn.id, imageId)
                }
                newImagesToUpload.entries.forEach { (imageId, imageData) ->
                    imageManager.saveYarnImage(editedYarn.id, imageId, imageData)
                }
                jsonDataManager.addOrUpdateYarn(editedYarn)
            }
            reloadAllData()
            onDone?.invoke()
        }
    }

    fun deleteYarn(yarnId: UInt) {
        scope.launch {
            try {
                withContext(Dispatchers.Default) {
                    val yarnToDelete = jsonDataManager.getYarnById(yarnId)
                    yarnToDelete!!.imageIds.forEach { imageId ->
                        imageManager.deleteYarnImage(yarnId, imageId)
                    }
                    jsonDataManager.deleteYarn(yarnId)
                }
                reloadAllData()
                removeScreensFromStack { it is Screen.YarnForm && it.yarnId == yarnId }
            } catch (e: Exception) {
                Logger.log(LogLevel.ERROR, "Failed to delete yarn with id $yarnId: ${e.message}", e)
                _errorDialogMessage.value = "Failed to delete yarn: ${e.message}"
            }
        }
    }

    fun addColorVariant(yarnToCopy: Yarn) {
        scope.launch {
            val newYarnWithNewId = jsonDataManager.createNewYarn(yarnToCopy.name)
            val newYarn = yarnToCopy.copyForColor().copy(id = newYarnWithNewId.id)
            withContext(Dispatchers.Default) { jsonDataManager.addOrUpdateYarn(newYarn) }
            reloadAllData()
            navigateTo(Screen.YarnForm(newYarn.id))
        }
    }

    // --- Project operations ---

    fun addProject(defaultName: String) {
        scope.launch {
            val newProject = jsonDataManager.createNewProject(defaultName)
            withContext(Dispatchers.Default) { jsonDataManager.addOrUpdateProject(newProject) }
            reloadAllData()
            navigateTo(Screen.ProjectForm(newProject.id))
        }
    }

    fun saveProject(editedProject: Project, newImages: Map<UInt, ByteArray>, onDone: (() -> Unit)? = null) {
        scope.launch {
            val existingProject = try {
                jsonDataManager.getProjectById(editedProject.id)
            } catch (e: Exception) {
                Logger.log(LogLevel.ERROR, "Failed to get project ${editedProject.id} for save: ${e.message}", e)
                null
            }
            val existingImageIds = existingProject?.imageIds ?: emptyList()
            val newImagesToUpload = newImages.filter { it.key !in existingImageIds }
            val idsToDelete = existingImageIds.filter { it !in newImages.keys }

            withContext(Dispatchers.Default) {
                idsToDelete.forEach { imageId ->
                    imageManager.deleteProjectImage(editedProject.id, imageId)
                }
                newImagesToUpload.entries.sortedBy { it.key }.forEach { (imageId, imageData) ->
                    imageManager.saveProjectImage(editedProject.id, imageId, imageData)
                }
                jsonDataManager.addOrUpdateProject(editedProject)
            }
            reloadAllData()
            onDone?.invoke()
        }
    }

    fun deleteProject(projectId: UInt) {
        scope.launch {
            try {
                withContext(Dispatchers.Default) {
                    val projectToDelete = jsonDataManager.getProjectById(projectId)
                    projectToDelete!!.imageIds.forEach { imageId ->
                        imageManager.deleteProjectImage(projectId, imageId)
                    }
                    jsonDataManager.deleteProject(projectId)
                }
                reloadAllData()
                removeScreensFromStack {
                    (it is Screen.ProjectForm && it.projectId == projectId) ||
                    (it is Screen.ProjectAssignments && it.projectId == projectId)
                }
            } catch (e: Exception) {
                Logger.log(LogLevel.ERROR, "Failed to delete project with id $projectId: ${e.message}", e)
                _errorDialogMessage.value = "Failed to delete project: ${e.message}"
            }
        }
    }

    fun setProjectAssignments(projectId: UInt, updatedAssignments: Map<UInt, Int>) {
        scope.launch {
            withContext(Dispatchers.Default) {
                jsonDataManager.setProjectAssignments(projectId, updatedAssignments)
            }
            reloadAllData()
        }
    }

    // --- Pattern operations ---

    fun addPattern() {
        scope.launch {
            val newPattern = jsonDataManager.createNewPattern()
            withContext(Dispatchers.Default) { jsonDataManager.addOrUpdatePattern(newPattern) }
            reloadAllData()
            navigateTo(Screen.PatternForm(newPattern.id))
        }
    }

    fun savePattern(editedPattern: Pattern, pdf: ByteArray?, initialPdf: ByteArray?) {
        scope.launch {
            withContext(Dispatchers.Default) {
                if (initialPdf == null && pdf != null ||
                    initialPdf != null && pdf == null ||
                    (initialPdf != null && pdf != null && !initialPdf.contentEquals(pdf))
                ) {
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
    }

    fun deletePattern(patternId: UInt) {
        scope.launch {
            _projects.value.filter { it.patternId == patternId }.forEach { projectToUpdate ->
                val updatedProject = projectToUpdate.copy(patternId = null)
                withContext(Dispatchers.Default) { jsonDataManager.addOrUpdateProject(updatedProject) }
            }
            withContext(Dispatchers.Default) {
                pdfManager.deletePatternPdf(patternId)
                jsonDataManager.deletePattern(patternId)
            }
            reloadAllData()
            removeScreensFromStack { it is Screen.PatternForm && it.patternId == patternId }
        }
    }

    // --- Settings operations ---

    fun changeSettings(newSettings: Settings) {
        scope.launch {
            withContext(Dispatchers.Default) { settingsManager.saveSettings(newSettings) }
            Logger.updateSettings(newSettings)
            _settings.value = newSettings
        }
    }

    fun changeLocale(newLocale: String) {
        val current = _settings.value ?: return
        val newSettings = current.copy(language = newLocale)
        scope.launch {
            withContext(Dispatchers.Default) { settingsManager.saveSettings(newSettings) }
            setAppLanguage(newLocale)
            Logger.updateSettings(newSettings)
            _settings.value = newSettings
        }
    }

    fun changeLengthUnit(newLengthUnit: LengthUnit) {
        val current = _settings.value ?: return
        val newSettings = current.copy(lengthUnit = newLengthUnit)
        scope.launch {
            withContext(Dispatchers.Default) { settingsManager.saveSettings(newSettings) }
            Logger.updateSettings(newSettings)
            _settings.value = newSettings
        }
    }

    fun changeLogLevel(newLogLevel: LogLevel) {
        val current = _settings.value ?: return
        val newSettings = current.copy(logLevel = newLogLevel)
        scope.launch {
            withContext(Dispatchers.Default) { settingsManager.saveSettings(newSettings) }
            Logger.updateSettings(newSettings)
            _settings.value = newSettings
        }
    }

    // --- Export / Import ---

    fun exportZip() {
        scope.launch {
            try {
                _isExporting.value = true
                Logger.log(LogLevel.INFO, "Starting ZIP export")
                val exportFileName = fileHandler.createTimestampedFileName("openyarnstash", "zip")
                val zipBytes = withContext(Dispatchers.Default) { fileHandler.zipFiles() }
                withContext(Dispatchers.Default) { fileDownloader.download(exportFileName, zipBytes, getContext()) }
                _isExporting.value = false
                _showExportSuccessDialog.value = true
                Logger.log(LogLevel.INFO, "ZIP export successful")
            } catch (e: Exception) {
                _isExporting.value = false
                _errorDialogMessage.value = "Failed to export: ${e.message}"
                Logger.log(LogLevel.ERROR, "ZIP export failed: ${e.message}", e)
            }
        }
    }

    fun importJson(fileContent: String) {
        scope.launch {
            try {
                withContext(Dispatchers.Default) { jsonDataManager.importData(fileContent) }
                reloadAllData()
                _snackbarEvents.emit("Import successful")
            } catch (e: Exception) {
                val errorMessage = "Failed to import data: ${e.message}. The data file might be corrupt."
                _errorDialogMessage.value = errorMessage
                Logger.log(LogLevel.ERROR, "Failed to import data in importJson: ${e.message}", e)
            }
        }
    }

    fun importZip(zipInputStream: Any) {
        scope.launch {
            var importSuccessful = false
            var backupFolderName: String? = null

            try {
                _isImporting.value = true
                Logger.log(LogLevel.INFO, "Starting ZIP import")

                backupFolderName = "backup"
                try {
                    fileHandler.deleteBackupDirectory(backupFolderName)
                } catch (_: Exception) { }
                fileHandler.renameFilesDirectory(backupFolderName)

                withContext(Dispatchers.Default) { fileHandler.unzipAndReplaceFiles(zipInputStream) }

                importSuccessful = true
                Logger.log(LogLevel.INFO, "ZIP import successful")
            } catch (e: Exception) {
                Logger.log(LogLevel.ERROR, "ZIP import failed: ${e.message}", e)

                var rollbackSuccessful = false
                try {
                    fileHandler.deleteFilesDirectory()
                    if (backupFolderName != null) {
                        fileHandler.restoreBackupDirectory(backupFolderName)
                        rollbackSuccessful = true
                    }
                } catch (rollbackException: Exception) {
                    Logger.log(LogLevel.ERROR, "CRITICAL: Rollback failed: ${rollbackException.message}", rollbackException)
                }

                _errorDialogMessage.value = if (rollbackSuccessful) {
                    "Failed to import ZIP: ${e.message}\n\nYour previous data has been restored."
                } else {
                    "Failed to import ZIP: ${e.message}\n\nWarning: Could not restore previous data."
                }
            } finally {
                Logger.log(LogLevel.INFO, "Reloading data from disk")
                try {
                    reloadAllData()
                } catch (e: Exception) {
                    Logger.log(LogLevel.ERROR, "Failed to reload data after import: ${e.message}", e)
                    importSuccessful = false

                    var rollbackSuccessful = false
                    try {
                        fileHandler.deleteFilesDirectory()
                        if (backupFolderName != null) {
                            fileHandler.restoreBackupDirectory(backupFolderName)
                            rollbackSuccessful = true
                        }
                    } catch (rollbackException: Exception) {
                        Logger.log(LogLevel.ERROR, "CRITICAL: Rollback failed: ${rollbackException.message}", rollbackException)
                    }

                    if (_errorDialogMessage.value == null) {
                        _errorDialogMessage.value = if (rollbackSuccessful) {
                            "Failed to import ZIP: Data is corrupt or invalid.\n\nYour previous data has been restored."
                        } else if (backupFolderName != null) {
                            "Failed to import ZIP: Data is corrupt or invalid.\n\nWarning: Could not restore previous data."
                        } else {
                            "Failed to import ZIP: Data is corrupt or invalid.\n\nNote: No previous data existed to restore."
                        }
                    }
                }

                if (importSuccessful && backupFolderName != null) {
                    try {
                        fileHandler.deleteBackupDirectory(backupFolderName)
                    } catch (e: Exception) {
                        Logger.log(LogLevel.WARN, "Failed to delete temporary backup: ${e.message}", e)
                    }
                }

                _isImporting.value = false
                if (importSuccessful) {
                    _showImportSuccessDialog.value = true
                }
            }
        }
    }

    // --- Dialog dismissers ---

    fun dismissNotImplementedDialog() { _showNotImplementedDialog.value = false }
    fun dismissErrorDialog() { _errorDialogMessage.value = null }
    fun dismissDirtyBuildWarning() { _showDirtyBuildWarning.value = false }
    fun dismissFutureVersionWarning() { _showFutureVersionWarning.value = false }
    fun dismissExportSuccessDialog() { _showExportSuccessDialog.value = false }
    fun dismissImportSuccessDialog() { _showImportSuccessDialog.value = false }
}
