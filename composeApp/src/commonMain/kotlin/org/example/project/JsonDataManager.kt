package org.example.project

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Repository for managing yarns, projects, and their usages from a JSON file.
 */
class JsonDataManager(private val fileHandler: FileHandler, private val filePath: String = "stash.json") {

    private var data: AppData = AppData()

    /**
     * Loads data from the JSON file. If the file is empty or doesn't exist,
     * it initializes with default empty lists.
     */
    suspend fun load(): AppData {
        val content = fileHandler.readFile(filePath)
        data = if (content.isNotEmpty()) {
            try {
                Json.decodeFromString<AppData>(content)
            } catch (e: SerializationException) {
                // Re-throw the exception to be handled by the caller
                throw e
            } catch (e: Exception) {
                // Handle other exceptions
                throw e
            }
        } else {
            AppData()
        }
        return data
    }

    /**
     * Saves the current application data to the JSON file.
     */
    private suspend fun save() {
        val content = Json.encodeToString(data)
        fileHandler.writeFile(filePath, content)
    }

    /**
     * Provides the raw JSON content of the current data.
     */
    fun getRawJson(): String {
        return Json.encodeToString(data)
    }

    /**
     * Backs up the current data file.
     * @return The name of the backup file, or null if backup failed.
     */
    suspend fun backup(): String? {
        return fileHandler.backupFile(filePath)
    }


    /**
     * Validates the given JSON content, backs up the current data file, and then overwrites it with the new content.
     * If the content is invalid, it throws a SerializationException.
     */
    suspend fun importData(content: String) {
        // First, validate the new content. This will throw if content is corrupt.
        val newData = Json.decodeFromString<AppData>(content)

        // If validation is successful, then backup the existing file.
        fileHandler.backupFile(filePath)

        // Then, write the new content to the main file.
        fileHandler.writeFile(filePath, content)

        // Finally, update the in-memory data.
        data = newData
    }

    // ... (rest of the functions for yarn, project, and usage management)
    fun getYarnById(id: Int): Yarn? = data.yarns.firstOrNull { it.id == id }

    suspend fun addOrUpdateYarn(yarn: Yarn) {
        val index = data.yarns.indexOfFirst { it.id == yarn.id }
        if (index != -1) {
            data.yarns[index] = yarn
        } else {
            data.yarns.add(yarn)
        }
        save()
    }

    suspend fun deleteYarn(id: Int) {
        data.yarns.removeAll { it.id == id }
        data.usages.removeAll { it.yarnId == id }
        save()
    }

    fun getProjectById(id: Int): Project? = data.projects.firstOrNull { it.id == id }

    suspend fun addOrUpdateProject(project: Project) {
        val index = data.projects.indexOfFirst { it.id == project.id }
        if (index != -1) {
            data.projects[index] = project
        } else {
            data.projects.add(project)
        }
        save()
    }

    suspend fun deleteProject(id: Int) {
        data.projects.removeAll { it.id == id }
        data.usages.removeAll { it.projectId == id }
        save()
    }

    fun availableForYarn(yarnId: Int, forProjectId: Int? = null): Int {
        val yarn = getYarnById(yarnId) ?: return 0
        val used = data.usages
            .filter { it.yarnId == yarnId && it.projectId != forProjectId }
            .sumOf { it.amount }
        return yarn.amount - used
    }

    suspend fun setProjectAssignments(projectId: Int, assignments: Map<Int, Int>) {
        // Remove existing assignments for this project
        data.usages.removeAll { it.projectId == projectId }

        // Add new assignments
        for ((yarnId, amount) in assignments) {
            if (amount > 0) {
                val usage = Usage(yarnId = yarnId, projectId = projectId, amount = amount)
                data.usages.add(usage)
            }
        }
        save()
    }
}
