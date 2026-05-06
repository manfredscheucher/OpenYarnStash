package org.example.project

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

// =============================================================================
// NOT COVERED BY AUTOMATED TESTS (requires UI / platform / hardware):
//   - Statistics chart rendering (bar chart, axis labels, visual layout)
//   - PDF viewer / PDF thumbnail rendering in PatternListScreen
//   - Camera capture (hardware sensor)
//   - Full-screen image zoom gesture (pinch/pan — Compose gesture layer)
//   - Image picker file dialog (platform file chooser)
//   - ZIP export file download dialog (platform save dialog)
//   - Snackbar display timing and animation
//   - Language switching (requires locale/resource reload)
//   - Unsaved-changes dialog flow (UI interaction)
// =============================================================================

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

    // Minimal valid PNG (1x1 red pixel) for image tests
    private fun fakePngBytes(seed: Byte = 0): ByteArray {
        // 1x1 PNG: valid PNG header + minimal IHDR/IDAT/IEND
        // Using a known-good minimal PNG
        val base = byteArrayOf(
            -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82,
            0, 0, 0, 1, 0, 0, 0, 1, 8, 2, 0, 0, 0, -112, 119, 83, -34, 0, 0,
            0, 12, 73, 68, 65, 84, 8, -41, 99, -8, -49, -64, 0, 0, 0, 2, 0, 1,
            -30, 33, -68, 51, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
        )
        // XOR last byte with seed so images are distinguishable
        base[base.size - 1] = (base[base.size - 1].toInt() xor seed.toInt()).toByte()
        return base
    }

    // =========================================================================
    // Navigation
    // =========================================================================

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
    fun removeScreensFromStackFiltersCorrectly() {
        runBlocking {
            val appState = makeAppState(this)
            appState.navigateTo(Screen.YarnList)
            appState.navigateTo(Screen.ProjectList)
            appState.navigateTo(Screen.YarnList)
            appState.removeScreensFromStack { it == Screen.YarnList }
            assertEquals(listOf(Screen.Home, Screen.ProjectList), appState.navStack.value)
        }
    }

    // =========================================================================
    // Yarn — basic CRUD
    // =========================================================================

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

    // =========================================================================
    // Yarn — all fields persist through save/reload
    // =========================================================================

    @Test
    fun saveYarnPersistsAllFields() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addYarn("Base")
            awaitAllJobs()
            val base = appState.yarns.value.first()
            val updated = base.copy(
                name = "Merino Superwash",
                brand = "Drops",
                color = "Dusty Rose",
                colorCode = "#C4A0A0",
                blend = "100% Merino",
                dyeLot = "DL-42",
                storagePlace = "Shelf B",
                amount = 400,
                weightPerSkein = 50,
                meteragePerSkein = 200,
                notes = "Very soft, good for socks"
            )
            appState.saveYarn(updated, emptyMap())
            awaitAllJobs()
            val saved = appState.yarns.value.first()
            assertEquals("Merino Superwash", saved.name)
            assertEquals("Drops", saved.brand)
            assertEquals("Dusty Rose", saved.color)
            assertEquals("#C4A0A0", saved.colorCode)
            assertEquals("100% Merino", saved.blend)
            assertEquals("DL-42", saved.dyeLot)
            assertEquals("Shelf B", saved.storagePlace)
            assertEquals(400, saved.amount)
            assertEquals(50, saved.weightPerSkein)
            assertEquals(200, saved.meteragePerSkein)
            assertEquals("Very soft, good for socks", saved.notes)
        }
    }

    @Test
    fun yarnAvailableAmountReflectsAmountMinusUsed() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addYarn("Wool")
            awaitAllJobs()
            val yarn = appState.yarns.value.first()
            appState.saveYarn(yarn.copy(amount = 300), emptyMap())
            awaitAllJobs()
            // No assignments yet — availableAmount == amount
            assertEquals(300, appState.yarns.value.first().availableAmount)
        }
    }

    @Test
    fun yarnMeterageCalculationsAreCorrect() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addYarn("Sock Yarn")
            awaitAllJobs()
            val yarn = appState.yarns.value.first()
            // 100g total, 50g used, 200m per 100g
            val updated = yarn.copy(amount = 100, weightPerSkein = 100, meteragePerSkein = 200)
            appState.saveYarn(updated, emptyMap())
            awaitAllJobs()
            val saved = appState.yarns.value.first()
            // usedAmount is @Transient = 0 here (no assignments)
            assertEquals(200, saved.availableMeterage)
        }
    }

    // =========================================================================
    // Yarn — images (add, delete, reorder persisted in imageIds)
    // =========================================================================

    @Test
    fun saveYarnWithImageStoresImageId() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addYarn("Yarn With Image")
            awaitAllJobs()
            val yarn = appState.yarns.value.first()
            val imgId = 1u
            val newImages = mapOf(imgId to fakePngBytes(1))
            appState.saveYarn(yarn.copy(imageIds = listOf(imgId)), newImages)
            awaitAllJobs()
            val saved = appState.yarns.value.first()
            assertEquals(listOf(imgId), saved.imageIds)
        }
    }

    @Test
    fun saveYarnDeletesRemovedImage() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addYarn("Yarn")
            awaitAllJobs()
            val yarn = appState.yarns.value.first()
            // First: save with one image
            val imgId = 1u
            appState.saveYarn(yarn.copy(imageIds = listOf(imgId)), mapOf(imgId to fakePngBytes()))
            awaitAllJobs()
            assertEquals(1, appState.yarns.value.first().imageIds.size)
            // Then: save with no images → should delete
            appState.saveYarn(appState.yarns.value.first().copy(imageIds = emptyList()), emptyMap())
            awaitAllJobs()
            assertTrue(appState.yarns.value.first().imageIds.isEmpty())
        }
    }

    @Test
    fun saveYarnPreservesImageOrder() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addYarn("Yarn")
            awaitAllJobs()
            val yarn = appState.yarns.value.first()
            val ids = listOf(3u, 1u, 2u) // non-sorted order
            val images = mapOf(1u to fakePngBytes(1), 2u to fakePngBytes(2), 3u to fakePngBytes(3))
            appState.saveYarn(yarn.copy(imageIds = ids), images)
            awaitAllJobs()
            assertEquals(ids, appState.yarns.value.first().imageIds)
        }
    }

    @Test
    fun saveYarnWithMultipleImagesThenReorderPersists() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addYarn("Yarn")
            awaitAllJobs()
            val yarn = appState.yarns.value.first()
            val images = mapOf(1u to fakePngBytes(1), 2u to fakePngBytes(2), 3u to fakePngBytes(3))
            // Initial order: 1, 2, 3
            appState.saveYarn(yarn.copy(imageIds = listOf(1u, 2u, 3u)), images)
            awaitAllJobs()
            assertEquals(listOf(1u, 2u, 3u), appState.yarns.value.first().imageIds)
            // Reorder to: 3, 1, 2
            appState.saveYarn(appState.yarns.value.first().copy(imageIds = listOf(3u, 1u, 2u)), images)
            awaitAllJobs()
            assertEquals(listOf(3u, 1u, 2u), appState.yarns.value.first().imageIds)
        }
    }

    // =========================================================================
    // Yarn — color variant
    // =========================================================================

    @Test
    fun addColorVariantCopiesCorrectFields() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addYarn("Original")
            awaitAllJobs()
            val original = appState.yarns.value.first()
            val withFields = original.copy(
                name = "Cosy Wool",
                brand = "Drops",
                blend = "80% Wool, 20% Nylon",
                dyeLot = "DL-99",
                storagePlace = "Bin 3",
                notes = "Birthday gift yarn",
                weightPerSkein = 100,
                meteragePerSkein = 400,
                color = "Red",         // color should NOT be copied
                colorCode = "#FF0000", // colorCode should NOT be copied
                amount = 500           // amount should NOT be copied
            )
            appState.saveYarn(withFields, emptyMap())
            awaitAllJobs()
            appState.addColorVariant(appState.yarns.value.first())
            awaitAllJobs()
            assertEquals(2, appState.yarns.value.size)
            val variant = appState.yarns.value.first { it.id != withFields.id }
            assertEquals("Cosy Wool", variant.name)
            assertEquals("Drops", variant.brand)
            assertEquals("80% Wool, 20% Nylon", variant.blend)
            assertEquals("DL-99", variant.dyeLot)
            assertEquals("Bin 3", variant.storagePlace)
            assertEquals("Birthday gift yarn", variant.notes)
            assertEquals(100, variant.weightPerSkein)
            assertEquals(400, variant.meteragePerSkein)
            // These must NOT be copied
            assertNull(variant.color)
            assertNull(variant.colorCode)
            assertEquals(0, variant.amount)
        }
    }

    // =========================================================================
    // Project — basic CRUD
    // =========================================================================

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

    // =========================================================================
    // Project — all fields persist
    // =========================================================================

    @Test
    fun saveProjectPersistsAllFields() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addProject("Base")
            awaitAllJobs()
            val base = appState.projects.value.first()
            val updated = base.copy(
                name = "Christmas Sweater",
                madeFor = "Grandma",
                startDate = "2024-01-01",
                endDate = "2024-12-24",
                needleSize = "4.0mm",
                size = "M",
                gauge = "22 sts / 10cm",
                notes = "Use circular needles",
                rowCounters = listOf(RowCounter("Main", 42), RowCounter("Border", 8))
            )
            appState.saveProject(updated, emptyMap())
            awaitAllJobs()
            val saved = appState.projects.value.first()
            assertEquals("Christmas Sweater", saved.name)
            assertEquals("Grandma", saved.madeFor)
            assertEquals("2024-01-01", saved.startDate)
            assertEquals("2024-12-24", saved.endDate)
            assertEquals("4.0mm", saved.needleSize)
            assertEquals("M", saved.size)
            assertEquals("22 sts / 10cm", saved.gauge)
            assertEquals("Use circular needles", saved.notes)
            assertEquals(2, saved.rowCounters.size)
            assertEquals("Main", saved.rowCounters[0].name)
            assertEquals(42, saved.rowCounters[0].value)
            assertEquals("Border", saved.rowCounters[1].name)
            assertEquals(8, saved.rowCounters[1].value)
        }
    }

    @Test
    fun projectStatusDerivedFromDates() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addProject("P")
            awaitAllJobs()
            val base = appState.projects.value.first()

            // No dates → PLANNING
            appState.saveProject(base.copy(startDate = null, endDate = null), emptyMap())
            awaitAllJobs()
            assertEquals(ProjectStatus.PLANNING, appState.projects.value.first().status)

            // Start date only → IN_PROGRESS
            appState.saveProject(appState.projects.value.first().copy(startDate = "2024-01-01", endDate = null), emptyMap())
            awaitAllJobs()
            assertEquals(ProjectStatus.IN_PROGRESS, appState.projects.value.first().status)

            // Both dates → FINISHED
            appState.saveProject(appState.projects.value.first().copy(endDate = "2024-12-31"), emptyMap())
            awaitAllJobs()
            assertEquals(ProjectStatus.FINISHED, appState.projects.value.first().status)
        }
    }

    // =========================================================================
    // Project — images
    // =========================================================================

    @Test
    fun saveProjectWithImageStoresImageId() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addProject("Project With Image")
            awaitAllJobs()
            val project = appState.projects.value.first()
            val imgId = 1u
            appState.saveProject(project.copy(imageIds = listOf(imgId)), mapOf(imgId to fakePngBytes()))
            awaitAllJobs()
            assertEquals(listOf(imgId), appState.projects.value.first().imageIds)
        }
    }

    @Test
    fun saveProjectDeletesRemovedImage() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addProject("Project")
            awaitAllJobs()
            val project = appState.projects.value.first()
            appState.saveProject(project.copy(imageIds = listOf(1u)), mapOf(1u to fakePngBytes()))
            awaitAllJobs()
            appState.saveProject(appState.projects.value.first().copy(imageIds = emptyList()), emptyMap())
            awaitAllJobs()
            assertTrue(appState.projects.value.first().imageIds.isEmpty())
        }
    }

    @Test
    fun saveProjectPreservesImageOrder() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addProject("Project")
            awaitAllJobs()
            val project = appState.projects.value.first()
            val ids = listOf(3u, 1u, 2u)
            val images = mapOf(1u to fakePngBytes(1), 2u to fakePngBytes(2), 3u to fakePngBytes(3))
            appState.saveProject(project.copy(imageIds = ids), images)
            awaitAllJobs()
            assertEquals(ids, appState.projects.value.first().imageIds)
        }
    }

    // =========================================================================
    // Project — assignments and usedAmount
    // =========================================================================

    @Test
    fun setProjectAssignmentsPersistsAndUpdatesUsedAmount() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addYarn("Wool")
            awaitAllJobs()
            appState.addProject("Sweater")
            awaitAllJobs()
            val yarnId = appState.yarns.value.first().id
            val projectId = appState.projects.value.first().id
            appState.saveYarn(appState.yarns.value.first().copy(amount = 500), emptyMap())
            awaitAllJobs()

            appState.setProjectAssignments(projectId, mapOf(yarnId to 150))
            awaitAllJobs()

            val assignment = appState.assignments.value.first()
            assertEquals(yarnId, assignment.yarnId)
            assertEquals(projectId, assignment.projectId)
            assertEquals(150, assignment.amount)

            // Note: usedAmount is @Transient and computed in the UI layer (YarnListScreen),
            // not stored in AppState — so we only verify the assignment record here.
        }
    }

    @Test
    fun setProjectAssignmentsReplacesExistingAssignments() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addYarn("Wool A")
            awaitAllJobs()
            appState.addYarn("Wool B")
            awaitAllJobs()
            appState.addProject("Hat")
            awaitAllJobs()
            val yarnAId = appState.yarns.value[0].id
            val yarnBId = appState.yarns.value[1].id
            val projectId = appState.projects.value.first().id
            appState.saveYarn(appState.yarns.value[0].copy(amount = 200), emptyMap())
            appState.saveYarn(appState.yarns.value[1].copy(amount = 200), emptyMap())
            awaitAllJobs()

            // First assignment: yarnA = 100
            appState.setProjectAssignments(projectId, mapOf(yarnAId to 100))
            awaitAllJobs()
            assertEquals(1, appState.assignments.value.size)

            // Replace with: yarnB = 80 (yarnA should be removed)
            appState.setProjectAssignments(projectId, mapOf(yarnBId to 80))
            awaitAllJobs()
            assertEquals(1, appState.assignments.value.size)
            assertEquals(yarnBId, appState.assignments.value.first().yarnId)
            assertEquals(80, appState.assignments.value.first().amount)
        }
    }

    @Test
    fun deleteProjectAlsoRemovesAssignments() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addYarn("Wool")
            awaitAllJobs()
            appState.addProject("Scarf")
            awaitAllJobs()
            val yarnId = appState.yarns.value.first().id
            val projectId = appState.projects.value.first().id
            appState.setProjectAssignments(projectId, mapOf(yarnId to 100))
            awaitAllJobs()
            assertTrue(appState.assignments.value.isNotEmpty())

            appState.deleteProject(projectId)
            awaitAllJobs()
            assertTrue(appState.assignments.value.isEmpty())
        }
    }

    // =========================================================================
    // Pattern — basic CRUD
    // =========================================================================

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
    fun savePatternPersistsAllFields() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addPattern()
            awaitAllJobs()
            val pattern = appState.patterns.value.first()
            val updated = pattern.copy(
                name = "Featherweight Cardigan",
                creator = "Bristol Ivy",
                category = "Garments",
                gauge = "24 sts / 10cm"
            )
            appState.savePattern(updated, null, null)
            awaitAllJobs()
            val saved = appState.patterns.value.first()
            assertEquals("Featherweight Cardigan", saved.name)
            assertEquals("Bristol Ivy", saved.creator)
            assertEquals("Garments", saved.category)
            assertEquals("24 sts / 10cm", saved.gauge)
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
    fun deletePatternClearsPatternIdFromProjects() {
        runBlocking {
            val appState = makeAppState(this)
            appState.addPattern()
            awaitAllJobs()
            val patternId = appState.patterns.value.first().id
            appState.addProject("My Project")
            awaitAllJobs()
            val project = appState.projects.value.first()
            appState.saveProject(project.copy(patternId = patternId), emptyMap())
            awaitAllJobs()
            assertEquals(patternId, appState.projects.value.first().patternId)

            appState.deletePattern(patternId)
            awaitAllJobs()
            assertNull(appState.projects.value.first().patternId)
        }
    }

    // =========================================================================
    // Settings
    // =========================================================================

    @Test
    fun changeSettingsUpdatesSettingsState() {
        runBlocking {
            val appState = makeAppState(this)
            appState.changeSettings(Settings(language = "en", lengthUnit = LengthUnit.METER))
            awaitAllJobs()
            appState.changeSettings(Settings(language = "en", lengthUnit = LengthUnit.YARD))
            awaitAllJobs()
            assertEquals(LengthUnit.YARD, appState.settings.value?.lengthUnit)
        }
    }

    @Test
    fun changeLengthUnitUpdatesSettings() {
        runBlocking {
            val appState = makeAppState(this)
            appState.changeSettings(Settings(lengthUnit = LengthUnit.METER))
            awaitAllJobs()
            appState.changeLengthUnit(LengthUnit.YARD)
            awaitAllJobs()
            assertEquals(LengthUnit.YARD, appState.settings.value?.lengthUnit)
        }
    }

    @Test
    fun changeLogLevelUpdatesSettings() {
        runBlocking {
            val appState = makeAppState(this)
            appState.changeSettings(Settings())
            awaitAllJobs()
            appState.changeLogLevel(LogLevel.DEBUG)
            awaitAllJobs()
            assertEquals(LogLevel.DEBUG, appState.settings.value?.logLevel)
        }
    }

    @Test
    fun changeLocaleUpdatesSettings() {
        runBlocking {
            val appState = makeAppState(this)
            appState.changeSettings(Settings(language = "en"))
            awaitAllJobs()
            appState.changeLocale("de")
            awaitAllJobs()
            assertEquals("de", appState.settings.value?.language)
        }
    }

    // =========================================================================
    // Dialog dismissers
    // =========================================================================

    @Test
    fun dismissErrorDialogClearsMessage() {
        runBlocking {
            val appState = makeAppState(this)
            appState.dismissErrorDialog()
            assertNull(appState.errorDialogMessage.value)
        }
    }

    @Test
    fun dismissDirtyBuildWarningClearsFlag() {
        runBlocking {
            val appState = makeAppState(this)
            appState.dismissDirtyBuildWarning()
            assertFalse(appState.showDirtyBuildWarning.value)
        }
    }

    @Test
    fun dismissFutureVersionWarningClearsFlag() {
        runBlocking {
            val appState = makeAppState(this)
            appState.dismissFutureVersionWarning()
            assertFalse(appState.showFutureVersionWarning.value)
        }
    }

    @Test
    fun dismissExportSuccessDialogClearsFlag() {
        runBlocking {
            val appState = makeAppState(this)
            appState.dismissExportSuccessDialog()
            assertFalse(appState.showExportSuccessDialog.value)
        }
    }

    @Test
    fun dismissImportSuccessDialogClearsFlag() {
        runBlocking {
            val appState = makeAppState(this)
            appState.dismissImportSuccessDialog()
            assertFalse(appState.showImportSuccessDialog.value)
        }
    }
}
