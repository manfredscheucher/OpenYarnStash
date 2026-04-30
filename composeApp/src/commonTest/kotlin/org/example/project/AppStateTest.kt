package org.example.project

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AppStateTest {

    private fun makeAppState(scope: CoroutineScope): AppState {
        val fileHandler = createPlatformFileHandler()
        Logger.init(fileHandler, Settings())
        val jsonDataManager = JsonDataManager(fileHandler)
        val imageManager = ImageManager(fileHandler)
        val settingsManager = JsonSettingsManager(fileHandler, "settings.json")
        val fileDownloader = FileDownloader()
        val pdfManager = createPdfManager(fileHandler)
        return AppState(jsonDataManager, imageManager, settingsManager, fileHandler, fileDownloader, pdfManager, scope)
    }

    // Wait for all child coroutines launched via scope.launch in AppState
    // to finish (including their Dispatchers.Default sub-tasks).
    private suspend fun CoroutineScope.awaitAllJobs() {
        coroutineContext.job.children.toList().forEach { it.join() }
    }

    @Test
    fun initialScreenIsHome() {
        runBlocking {
            val appState = makeAppState(this)
            assertEquals(listOf<Screen>(Screen.Home), appState.navStack.value)
            assertEquals(Screen.Home, appState.currentScreen)
        }
    }

    @Test
    fun navigateToAddsScreenToStack() {
        runBlocking {
            val appState = makeAppState(this)
            appState.navigateTo(Screen.YarnList)
            assertEquals(listOf(Screen.Home, Screen.YarnList), appState.navStack.value)
        }
    }

    @Test
    fun navigateBackRemovesLastScreen() {
        runBlocking {
            val appState = makeAppState(this)
            appState.navigateTo(Screen.YarnList)
            appState.navigateBack()
            assertEquals(listOf<Screen>(Screen.Home), appState.navStack.value)
        }
    }

    @Test
    fun navigateBackDoesNothingAtRoot() {
        runBlocking {
            val appState = makeAppState(this)
            appState.navigateBack()
            assertEquals(listOf<Screen>(Screen.Home), appState.navStack.value)
        }
    }

    @Test
    fun addYarnCreatesYarnAndNavigatesToYarnForm() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addYarn("Test Yarn")
            awaitAllJobs()
            assertEquals(1, appState.yarns.value.size)
            assertIs<Screen.YarnForm>(appState.currentScreen)
        }
    }

    @Test
    fun saveYarnPersistsNameInYarnsList() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addYarn("Initial Name")
            awaitAllJobs()
            val yarn = appState.yarns.value.first()
            appState.saveYarn(yarn.copy(name = "Updated Name"), emptyMap())
            awaitAllJobs()
            assertEquals("Updated Name", appState.yarns.value.first().name)
        }
    }

    @Test
    fun deleteYarnRemovesFromListAndPopsNavStack() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addYarn("To Delete")
            awaitAllJobs()
            val yarnId = appState.yarns.value.first().id
            appState.deleteYarn(yarnId)
            awaitAllJobs()
            assertTrue(appState.yarns.value.isEmpty())
            assertEquals(Screen.Home, appState.currentScreen)
        }
    }

    @Test
    fun addProjectCreatesProjectAndNavigatesToProjectForm() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addProject("Test Project")
            awaitAllJobs()
            assertEquals(1, appState.projects.value.size)
            assertIs<Screen.ProjectForm>(appState.currentScreen)
        }
    }

    @Test
    fun saveProjectPersistsNameInProjectsList() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addProject("Initial Project")
            awaitAllJobs()
            val project = appState.projects.value.first()
            appState.saveProject(project.copy(name = "Renamed Project"), emptyMap())
            awaitAllJobs()
            assertEquals("Renamed Project", appState.projects.value.first().name)
        }
    }

    @Test
    fun deleteProjectRemovesFromListAndPopsNavStack() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addProject("To Delete")
            awaitAllJobs()
            val projectId = appState.projects.value.first().id
            appState.deleteProject(projectId)
            awaitAllJobs()
            assertTrue(appState.projects.value.isEmpty())
            assertEquals(Screen.Home, appState.currentScreen)
        }
    }

    @Test
    fun addPatternCreatesPatternAndNavigatesToPatternForm() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addPattern()
            awaitAllJobs()
            assertEquals(1, appState.patterns.value.size)
            assertIs<Screen.PatternForm>(appState.currentScreen)
        }
    }

    @Test
    fun savePatternPersistsNameInPatternsList() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addPattern()
            awaitAllJobs()
            val pattern = appState.patterns.value.first()
            appState.savePattern(pattern.copy(name = "My Pattern"), null, null)
            awaitAllJobs()
            assertEquals("My Pattern", appState.patterns.value.first().name)
        }
    }

    @Test
    fun deletePatternRemovesFromListAndPopsNavStack() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addPattern()
            awaitAllJobs()
            val patternId = appState.patterns.value.first().id
            appState.deletePattern(patternId)
            awaitAllJobs()
            assertTrue(appState.patterns.value.isEmpty())
            assertEquals(Screen.Home, appState.currentScreen)
        }
    }

    @Test
    fun changeSettingsUpdatesSettingsState() {
        runBlocking {
            val appState = makeAppState(this)
            val initialSettings = Settings(language = "en", lengthUnit = LengthUnit.METER)
            appState.changeSettings(initialSettings)
            awaitAllJobs()
            appState.changeSettings(initialSettings.copy(lengthUnit = LengthUnit.YARD))
            awaitAllJobs()
            assertEquals(LengthUnit.YARD, appState.settings.value?.lengthUnit)
        }
    }
}
