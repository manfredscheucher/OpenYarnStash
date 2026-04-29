package org.example.project

import org.junit.Test

/**
 * End-to-end UI workflow tests for pattern CRUD operations.
 */
class PatternWorkflowTest : UiTestBase() {

    /**
     * Full create → verify → edit → verify → delete workflow for a pattern.
     */
    @Test
    fun createEditDeletePattern() = startFresh("pattern_create_edit_delete").let {

        // Navigate to pattern list
        waitForTag("btn_home_patterns")
        clickButton("btn_home_patterns")

        // Add new pattern
        waitForTag("btn_pattern_add")
        clickButton("btn_pattern_add")

        // Fill in pattern form
        waitForTag("field_pattern_name")
        fillField("field_pattern_name", "Aran Sweater")
        fillField("field_pattern_creator", "Drops Design")

        // Save pattern
        clickButton("btn_pattern_save")

        // Back on pattern list – pattern should appear
        waitForText("Aran Sweater")
        assertVisible("Aran Sweater")

        // Open pattern to verify fields
        clickItemWithText("Aran Sweater")
        waitForTag("field_pattern_name")
        assertFieldHasText("field_pattern_name", "Aran Sweater")
        assertFieldHasText("field_pattern_creator", "Drops Design")

        // Edit the name
        fillField("field_pattern_name", "Nordic Sweater")
        clickButton("btn_pattern_save")

        // Verify updated name in list
        waitForText("Nordic Sweater")
        assertVisible("Nordic Sweater")
        assertNotVisible("Aran Sweater")

        // Open again and delete
        clickItemWithText("Nordic Sweater")
        waitForTag("btn_pattern_delete")
        clickButton("btn_pattern_delete")

        // Pattern list should now be empty
        waitForTag("btn_pattern_add")
        assertNotVisible("Nordic Sweater")
    }

    /**
     * Verify that pattern fields persist after save and reopen.
     */
    @Test
    fun patternFieldsPersistAfterSave() = startFresh("pattern_fields_persist").let {

        waitForTag("btn_home_patterns")
        clickButton("btn_home_patterns")
        waitForTag("btn_pattern_add")
        clickButton("btn_pattern_add")

        waitForTag("field_pattern_name")
        fillField("field_pattern_name", "Cable Hat")
        fillField("field_pattern_creator", "Ravelry")

        clickButton("btn_pattern_save")

        waitForText("Cable Hat")
        clickItemWithText("Cable Hat")

        waitForTag("field_pattern_name")
        assertFieldHasText("field_pattern_name", "Cable Hat")
        assertFieldHasText("field_pattern_creator", "Ravelry")
    }
}
