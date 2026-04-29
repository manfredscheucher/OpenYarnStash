package org.example.project

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.io.File
import java.nio.file.Files

// performClick() doesn't trigger onClick on Compose Desktop – use mouse events instead.
// See: https://github.com/JetBrains/compose-multiplatform/issues/1216
private fun SemanticsNodeInteraction.desktopClick(): SemanticsNodeInteraction =
    apply { performMouseInput { click() } }

/**
 * Base class for all UI workflow tests.
 *
 * Sets up a fresh temporary directory for each test (startFresh),
 * provides DSL helpers for interacting with the app UI,
 * and optionally captures screenshots after each step.
 *
 * Usage:
 *   class MyTest : UiTestBase() {
 *       @Test fun myWorkflow() = runTest {
 *           clickButton("btn_home_yarns")
 *           clickButton("btn_yarn_add")
 *           fillField("field_yarn_name", "Merino")
 *           clickButton("btn_yarn_save")
 *           assertVisible("Merino")
 *       }
 *   }
 */
abstract class UiTestBase {

    @get:Rule
    val composeRule: ComposeContentTestRule = createComposeRule()

    lateinit var tempDir: File
    private var stepCounter = 0
    private var currentTestName = "test"

    /** Set to false to skip screenshot capture (faster runs). */
    open val captureScreenshots: Boolean = true

    @Before
    fun setupTest() {
        tempDir = Files.createTempDirectory("openyarnstash_test_").toFile()
        stepCounter = 0
    }

    @After
    fun teardownTest() {
        tempDir.deleteRecursively()
    }

    /**
     * Launches the full App with a fresh empty data directory.
     * Call this at the start of every test.
     */
    fun startFresh(testName: String = "test") {
        currentTestName = testName
        val fileHandler = JvmFileHandler(tempDir)
        val jsonDataManager = JsonDataManager(fileHandler, "stash.json")
        val imageManager = ImageManager(fileHandler)
        val settingsManager = JsonSettingsManager(fileHandler, "settings.json")
        val fileDownloader = FileDownloader()

        // Initialize logger pointing to temp dir
        Logger.init(fileHandler, settingsManager.settings)

        composeRule.setContent {
            val backDispatcher = remember { DesktopBackDispatcher() }
            CompositionLocalProvider(LocalDesktopBackDispatcher provides backDispatcher) {
                App(
                    jsonDataManager = jsonDataManager,
                    imageManager = imageManager,
                    fileDownloader = fileDownloader,
                    fileHandler = fileHandler,
                    settingsManager = settingsManager
                )
            }
        }

        // Wait for settings to load and HomeScreen to render (may take a few seconds due to async)
        // Retry up to 3 dialog dismissals to handle multiple startup warnings
        var attempts = 0
        while (attempts < 3) {
            composeRule.waitUntil(timeoutMillis = 15000) {
                composeRule.onAllNodesWithTag("btn_home_yarns").fetchSemanticsNodes().isNotEmpty()
                    || composeRule.onAllNodesWithText("OK", ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
            }
            if (composeRule.onAllNodesWithTag("btn_home_yarns").fetchSemanticsNodes().isNotEmpty()) break
            dismissStartupDialogsIfPresent()
            attempts++
        }
        screenshot("start")
    }

    /**
     * Clicks the OK button of any startup warning dialog that may appear.
     * These dialogs (dirty build, version warnings) must be dismissed before
     * interacting with the app in tests.
     */
    private fun dismissStartupDialogsIfPresent() {
        // Wait a moment for dialogs to appear
        composeRule.waitForIdle()
        // Try to click OK/dismiss on any dialog that might be showing
        // The dialogs use Text buttons with the common_ok string resource
        try {
            // Try common OK button text in English
            val okNodes = composeRule.onAllNodesWithText("OK", ignoreCase = true)
            if (okNodes.fetchSemanticsNodes().isNotEmpty()) {
                okNodes[0].desktopClick()
                composeRule.waitForIdle()
            }
        } catch (_: Exception) { }
    }

    // ─── DSL helpers ────────────────────────────────────────────────────────

    /** Click a node identified by testTag. */
    fun clickButton(tag: String) {
        composeRule.onNodeWithTag(tag).desktopClick()
        composeRule.waitForIdle()
        screenshot(tag)
    }

    /** Clear the field and type text into a node identified by testTag. */
    fun fillField(tag: String, text: String) {
        composeRule.onNodeWithTag(tag).also {
            it.desktopClick()
            it.performTextClearance()
            it.performTextInput(text)
        }
        composeRule.waitForIdle()
        screenshot("fill_${tag}")
    }

    /** Assert that a text string is visible somewhere on screen. */
    fun assertVisible(text: String) {
        composeRule.onNodeWithText(text).assertIsDisplayed()
    }

    /** Assert that a text string is NOT visible on screen. */
    fun assertNotVisible(text: String) {
        composeRule.onNodeWithText(text).assertDoesNotExist()
    }

    /** Assert that a specific field (by testTag) contains the expected text. */
    fun assertFieldHasText(tag: String, expected: String) {
        composeRule.onNodeWithTag(tag).assertTextContains(expected)
    }

    /** Click the first list item that contains the given text. */
    fun clickItemWithText(text: String) {
        composeRule.onNodeWithText(text).desktopClick()
        composeRule.waitForIdle()
        screenshot("click_$text")
    }

    /** Wait until a node with the given tag exists (useful after navigation). */
    fun waitForTag(tag: String, timeoutMs: Long = 10000) {
        composeRule.waitUntil(timeoutMillis = timeoutMs) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /** Wait until text appears on screen. */
    fun waitForText(text: String, timeoutMs: Long = 10000) {
        composeRule.waitUntil(timeoutMillis = timeoutMs) {
            composeRule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // ─── Screenshot support ──────────────────────────────────────────────────

    private fun screenshot(stepName: String) {
        if (!captureScreenshots) return
        stepCounter++
        val screenshotDir = File(System.getProperty("user.home"), "openyarnstash-test-screenshots/$currentTestName")
        screenshotDir.mkdirs()
        val fileName = "%03d_%s.png".format(stepCounter, stepName.replace(Regex("[^a-zA-Z0-9_]"), "_"))
        try {
            // useUnmergedTree=false + first root handles multiple root nodes (e.g. dialogs)
            val roots = composeRule.onAllNodes(androidx.compose.ui.test.isRoot())
            val imageBitmap = roots[0].captureToImage()
            val file = File(screenshotDir, fileName)
            // Convert ImageBitmap → AWT BufferedImage via pixel copy
            val width = imageBitmap.width
            val height = imageBitmap.height
            val pixels = IntArray(width * height)
            imageBitmap.readPixels(pixels, 0, width, 0, 0, width, height)
            val buffered = java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB)
            buffered.setRGB(0, 0, width, height, pixels, 0, width)
            javax.imageio.ImageIO.write(buffered, "PNG", file)
        } catch (_: Exception) {
            // Screenshot capture is best-effort – don't fail tests if it doesn't work
        }
    }
}
