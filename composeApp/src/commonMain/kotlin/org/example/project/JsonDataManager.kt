package org.example.project

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.random.Random

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
        val content = fileHandler.readText(filePath)
        data = if (content.isNotEmpty()) {
            try {
                val appData = Json.decodeFromString<AppData>(content)
                validateData(appData)
                appData
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
        fileHandler.writeText(filePath, content)
    }

    /**
     * Provides the raw JSON content of the current data.
     */
    suspend fun getRawJson(): String {
        return fileHandler.readText(filePath)
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
        validateData(newData)

        // If validation is successful, then backup the existing file.
        fileHandler.backupFile(filePath)

        // Then, write the new content to the main file.
        fileHandler.writeText(filePath, content)

        // Finally, update the in-memory data.
        data = newData
    }

    private fun validateData(appData: AppData) {
        val yarnIds = appData.yarns.map { it.id }.toSet()
        val projectIds = appData.projects.map { it.id }.toSet()
        val patternIds = appData.patterns.map { it.id }.toSet()

        for (yarn in appData.yarns) {
            if (yarn.name.isBlank()) {
                throw SerializationException("Yarn with id ${yarn.id} has a blank name.")
            }
        }

        for (project in appData.projects) {
            if (project.name.isBlank()) {
                throw SerializationException("Project with id ${project.id} has a blank name.")
            }
            if (project.patternId != null && !patternIds.contains(project.patternId)) {
                throw SerializationException("Project with id ${project.id} refers to a non-existent pattern with id ${project.patternId}.")
            }
        }

        for (pattern in appData.patterns) {
            if (pattern.name.isBlank()) {
                throw SerializationException("Pattern with id ${pattern.id} has a blank name.")
            }
        }

        for (usage in appData.usages) {
            if (!yarnIds.contains(usage.yarnId)) {
                throw SerializationException("Usage refers to a non-existent yarn with id ${usage.yarnId}.")
            }
            if (!projectIds.contains(usage.projectId)) {
                throw SerializationException("Usage refers to a non-existent project with id ${usage.projectId}.")
            }
        }
    }

    // ... (rest of the functions for yarn, project, and usage management)
    fun getYarnById(id: Int): Yarn? = data.yarns.firstOrNull { it.id == id }

    fun createNewYarn(defaultName: String): Yarn {
        val existingIds = data.yarns.map { it.id }.toSet()
        var newId: Int
        do {
            newId = Random.nextInt(1_000_000, 10_000_000)
        } while (existingIds.contains(newId))
        val yarnName = defaultName.replace("%1\$d", newId.toString())
        return Yarn(
            id = newId,
            name = yarnName,
            modified = getCurrentTimestamp()
        )
    }

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

    fun createNewProject(defaultName: String): Project {
        val existingIds = data.projects.map { it.id }.toSet()
        var newId: Int
        do {
            newId = Random.nextInt(1_000_000, 10_000_000)
        } while (existingIds.contains(newId))
        val projectName = defaultName.replace("%1\$d", newId.toString())
        return Project(
            id = newId,
            name = projectName,
            modified = getCurrentTimestamp()
        )
    }

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

    fun getPatternById(id: Int): Pattern? = data.patterns.firstOrNull { it.id == id }

    fun createNewPattern(): Pattern {
        val existingIds = data.patterns.map { it.id }.toSet()
        var newId: Int
        do {
            newId = Random.nextInt(1_000_000, 10_000_000)
        } while (existingIds.contains(newId))
        return Pattern(
            id = newId,
            name = "Pattern#$newId"
        )
    }

    suspend fun addOrUpdatePattern(pattern: Pattern) {
        val index = data.patterns.indexOfFirst { it.id == pattern.id }
        if (index != -1) {
            data.patterns[index] = pattern
        } else {
            data.patterns.add(pattern)
        }
        save()
    }

    suspend fun deletePattern(id: Int) {
        data.patterns.removeAll { it.id == id }
        data.projects.forEach { project ->
            if (project.patternId == id) {
                addOrUpdateProject(project.copy(patternId = null))
            }
        }
        save()
    }

    suspend fun updatePatternPdfId(patternId: Int, pdfId: Int?) {
        val index = data.patterns.indexOfFirst { it.id == patternId }
        if (index != -1) {
            data.patterns[index] = data.patterns[index].copy(pdfId = pdfId)
        }
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
