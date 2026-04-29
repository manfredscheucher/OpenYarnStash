package org.example.project

import org.junit.Test

/**
 * End-to-end UI workflow tests for yarn CRUD operations.
 *
 * Each test starts with a clean empty database (startFresh),
 * simulates real user interactions via the DSL, and verifies
 * the expected UI state after each step.
 *
 * Screenshots are saved to ~/openyarnstash-test-screenshots/<testName>/
 */
class YarnWorkflowTest : UiTestBase() {

    /**
     * Full create → verify → edit → verify → delete workflow for a yarn entry.
     *
     * Simulates:
     *   Home → Yarn List → Add → Fill Form → Save
     *   → verify in list → open → verify fields
     *   → edit name → save → verify changed
     *   → open → delete → verify gone
     */
    @Test
    fun createEditDeleteYarn() = startFresh("yarn_create_edit_delete").let {

        // Navigate to yarn list
        waitForTag("btn_home_yarns")
        clickButton("btn_home_yarns")

        // Yarn list should be empty – add new yarn
        waitForTag("btn_yarn_add")
        clickButton("btn_yarn_add")

        // Fill in yarn form
        waitForTag("field_yarn_name")
        fillField("field_yarn_brand", "Lang Yarns")
        fillField("field_yarn_name", "Merino Natura")
        fillField("field_yarn_color", "Blau")
        fillField("field_yarn_notes", "Sehr schöne Wolle")

        // Save yarn
        clickButton("btn_yarn_save")

        // Back on yarn list – yarn should appear
        waitForText("Merino Natura")
        assertVisible("Merino Natura")
        assertVisible("Lang Yarns")

        // Open yarn form again to verify fields are persisted
        clickItemWithText("Merino Natura")
        waitForTag("field_yarn_name")
        assertFieldHasText("field_yarn_name", "Merino Natura")
        assertFieldHasText("field_yarn_brand", "Lang Yarns")
        assertFieldHasText("field_yarn_color", "Blau")
        assertFieldHasText("field_yarn_notes", "Sehr schöne Wolle")

        // Edit the name
        fillField("field_yarn_name", "Merino Natura Updated")
        clickButton("btn_yarn_save")

        // Verify updated name in list
        waitForText("Merino Natura Updated")
        assertVisible("Merino Natura Updated")
        assertNotVisible("Merino Natura")

        // Open again and delete
        clickItemWithText("Merino Natura Updated")
        waitForTag("btn_yarn_delete")
        clickButton("btn_yarn_delete")

        // Confirm delete dialog
        waitForTag("btn_yarn_delete_confirm")
        clickButton("btn_yarn_delete_confirm")

        // Back on list – yarn should be gone
        waitForTag("btn_yarn_add")
        assertNotVisible("Merino Natura Updated")
    }

    /**
     * Verify that saving and reopening a yarn preserves all text fields.
     * This is especially important for the state-leak bug that was fixed
     * with remember(initial.id) in YarnFormScreen.
     */
    @Test
    fun yarnFormFieldsPersistAfterSave() = startFresh("yarn_fields_persist").let {

        waitForTag("btn_home_yarns")
        clickButton("btn_home_yarns")
        waitForTag("btn_yarn_add")
        clickButton("btn_yarn_add")

        waitForTag("field_yarn_name")
        fillField("field_yarn_brand", "Drops")
        fillField("field_yarn_name", "Alpaca")
        fillField("field_yarn_color", "Grau")
        fillField("field_yarn_notes", "100% Alpaka")
        fillField("field_yarn_storage_place", "Regal 3")

        clickButton("btn_yarn_save")

        // Navigate away and back
        waitForText("Alpaca")
        clickItemWithText("Alpaca")

        // All fields must still show the saved values
        waitForTag("field_yarn_name")
        assertFieldHasText("field_yarn_name", "Alpaca")
        assertFieldHasText("field_yarn_brand", "Drops")
        assertFieldHasText("field_yarn_color", "Grau")
        assertFieldHasText("field_yarn_notes", "100% Alpaka")
        assertFieldHasText("field_yarn_storage_place", "Regal 3")
    }

    /**
     * Verify that multiple yarns can be created and all appear in the list.
     */
    @Test
    fun multipleYarnsInList() = startFresh("yarn_multiple").let {

        waitForTag("btn_home_yarns")
        clickButton("btn_home_yarns")

        // Add first yarn
        waitForTag("btn_yarn_add")
        clickButton("btn_yarn_add")
        waitForTag("field_yarn_name")
        fillField("field_yarn_name", "Merino")
        clickButton("btn_yarn_save")

        // Add second yarn
        waitForTag("btn_yarn_add")
        clickButton("btn_yarn_add")
        waitForTag("field_yarn_name")
        fillField("field_yarn_name", "Cotton")
        clickButton("btn_yarn_save")

        // Both yarns must be in list
        waitForText("Merino")
        assertVisible("Merino")
        assertVisible("Cotton")
    }
}
