package org.example.project

import org.junit.Test

/**
 * End-to-end UI workflow tests for project CRUD operations.
 */
class ProjectWorkflowTest : UiTestBase() {

    /**
     * Full create → verify → edit → verify → delete workflow for a project.
     */
    @Test
    fun createEditDeleteProject() = startFresh("project_create_edit_delete").let {

        // Navigate to project list
        waitForTag("btn_home_projects")
        clickButton("btn_home_projects")

        // Add new project
        waitForTag("btn_project_add")
        clickButton("btn_project_add")

        // Fill in project form
        waitForTag("field_project_name")
        fillField("field_project_name", "Winterpullover")

        // Save project
        clickButton("btn_project_save")

        // Back on project list – project should appear
        waitForText("Winterpullover")
        assertVisible("Winterpullover")

        // Open project to verify field
        clickItemWithText("Winterpullover")
        waitForTag("field_project_name")
        assertFieldHasText("field_project_name", "Winterpullover")

        // Edit the name
        fillField("field_project_name", "Sommerpullover")
        clickButton("btn_project_save")

        // Verify updated name in list
        waitForText("Sommerpullover")
        assertVisible("Sommerpullover")
        assertNotVisible("Winterpullover")

        // Open again and delete
        clickItemWithText("Sommerpullover")
        waitForTag("btn_project_delete")
        clickButton("btn_project_delete")

        // Confirm delete dialog
        waitForTag("btn_project_delete_confirm")
        clickButton("btn_project_delete_confirm")

        // Back on list – project should be gone
        waitForTag("btn_project_add")
        assertNotVisible("Sommerpullover")
    }

    /**
     * Verify that project name persists after save and reopen.
     */
    @Test
    fun projectNamePersistsAfterSave() = startFresh("project_name_persists").let {

        waitForTag("btn_home_projects")
        clickButton("btn_home_projects")
        waitForTag("btn_project_add")
        clickButton("btn_project_add")

        waitForTag("field_project_name")
        fillField("field_project_name", "Mein Projekt")
        clickButton("btn_project_save")

        waitForText("Mein Projekt")
        clickItemWithText("Mein Projekt")

        waitForTag("field_project_name")
        assertFieldHasText("field_project_name", "Mein Projekt")
    }
}
