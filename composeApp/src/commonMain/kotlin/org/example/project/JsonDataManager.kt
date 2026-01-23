package org.example.project

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Repository for managing yarns, projects, and their assignments from a JSON file.
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

                // Filter out deleted items
                val filteredData = appData.copy(
                    yarns = appData.yarns.filter { it.deleted != true }.toMutableList(),
                    projects = appData.projects.filter { it.deleted != true }.toMutableList(),
                    patterns = appData.patterns.filter { it.deleted != true }.toMutableList(),
                    assignments = appData.assignments.filter { it.deleted != true }.toMutableList()
                )

                Logger.log(LogLevel.INFO, "Loaded data: ${filteredData.yarns.size} yarns, ${filteredData.projects.size} projects, ${filteredData.assignments.size} assignments, ${filteredData.patterns.size} patterns (filtered out deleted items)")
                filteredData
            } catch (e: SerializationException) {
                Logger.log(LogLevel.ERROR, "Failed to decode JSON data in fun load: ${e.message}", e)
                // Re-throw the exception to be handled by the caller
                throw e
            } catch (e: Exception) {
                Logger.log(LogLevel.ERROR, "Failed to load data in fun load: ${e.message}", e)
                // Handle other exceptions
                throw e
            }
        } else {
            Logger.log(LogLevel.INFO, "No existing data file found, initialized empty AppData")
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
        return fileHandler.backupFile(filePath).also { backupName ->
            if (backupName != null) {
                Logger.log(LogLevel.INFO, "Created backup: $backupName")
            } else {
                Logger.log(LogLevel.WARN, "Backup failed")
            }
        }
    }


    /**
     * Validates the given JSON content, backs up the current data file, and then overwrites it with the new content.
     * If the content is invalid, it throws a SerializationException.
     */
    suspend fun importData(content: String) {
        // First, validate the new content. This will throw if content is corrupt.
        val newData = Json.decodeFromString<AppData>(content)
        validateData(newData)
        Logger.log(LogLevel.INFO, "Import validation successful: ${newData.yarns.size} yarns, ${newData.projects.size} projects, ${newData.assignments.size} assignments, ${newData.patterns.size} patterns")

        // If validation is successful, then backup the existing file.
        val backupName = fileHandler.backupFile(filePath)
        Logger.log(LogLevel.INFO, "Created backup before import: $backupName")

        // Then, write the new content to the main file.
        fileHandler.writeText(filePath, content)

        // Finally, update the in-memory data.
        data = newData
        Logger.log(LogLevel.INFO, "Import completed successfully")
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

        for (assignment in appData.assignments) {
            if (!yarnIds.contains(assignment.yarnId)) {
                throw SerializationException("Assignment refers to a non-existent yarn with id ${assignment.yarnId}.")
            }
            if (!projectIds.contains(assignment.projectId)) {
                throw SerializationException("Assignment refers to a non-existent project with id ${assignment.projectId}.")
            }
        }
    }

    // ... (rest of the functions for yarn, project, and usage management)
    fun getYarnById(id: Int): Yarn? = data.yarns.firstOrNull { it.id == id && it.deleted != true }

    fun createNewYarn(defaultName: String): Yarn {
        val existingIds = data.yarns.map { it.id }.toSet()
        var newId: Int
        do {
            newId = Random.nextInt(until = 2_000_000_000)
        } while (existingIds.contains(newId))
        val yarnName = defaultName.replace("%1\$d", newId.toString())
        return Yarn(
            id = newId,
            name = yarnName,
            modified = getCurrentTimestamp()
        ).also {
            GlobalScope.launch {
                Logger.log(LogLevel.INFO, "Created new yarn: id=${it.id}, name=${it.name}")
            }
        }
    }

    suspend fun addOrUpdateYarn(yarn: Yarn) {
        val index = data.yarns.indexOfFirst { it.id == yarn.id }
        if (index != -1) {
            Logger.log(LogLevel.INFO, "Updated yarn: id=${yarn.id}, name=${yarn.name}")
            data.yarns[index] = yarn
        } else {
            Logger.log(LogLevel.INFO, "Added yarn: id=${yarn.id}, name=${yarn.name}")
            data.yarns.add(yarn)
        }
        save()
    }

    suspend fun deleteYarn(id: Int) {
        val index = data.yarns.indexOfFirst { it.id == id }
        if (index != -1) {
            val yarn = data.yarns[index]
            data.yarns[index] = yarn.copy(
                deleted = true,
                modified = getCurrentTimestamp()
            )

            // Also mark related assignments as deleted
            val timestamp = getCurrentTimestamp()
            val assignmentsCount = data.assignments.count { it.yarnId == id && it.deleted != true }
            data.assignments.forEachIndexed { idx, assignment ->
                if (assignment.yarnId == id && assignment.deleted != true) {
                    data.assignments[idx] = assignment.copy(
                        deleted = true,
                        lastModified = timestamp
                    )
                }
            }
            Logger.log(LogLevel.INFO, "Marked yarn as deleted: id=$id, name=${yarn.name}, marked $assignmentsCount assignments as deleted")
            save()
        }
    }

    fun getProjectById(id: Int): Project? = data.projects.firstOrNull { it.id == id && it.deleted != true }

    fun createNewProject(defaultName: String): Project {
        val existingIds = data.projects.map { it.id }.toSet()
        var newId: Int
        do {
            newId = Random.nextInt(until = 2_000_000_000)
        } while (existingIds.contains(newId))
        val projectName = defaultName.replace("%1\$d", newId.toString())
        return Project(
            id = newId,
            name = projectName,
            modified = getCurrentTimestamp()
        ).also {
            GlobalScope.launch {
                Logger.log(LogLevel.INFO, "Created new project: id=${it.id}, name=${it.name}")
            }
        }
    }

    suspend fun addOrUpdateProject(project: Project) {
        val index = data.projects.indexOfFirst { it.id == project.id }
        if (index != -1) {
            Logger.log(LogLevel.INFO, "Updated project: id=${project.id}, name=${project.name}")
            data.projects[index] = project
        } else {
            Logger.log(LogLevel.INFO, "Added project: id=${project.id}, name=${project.name}")
            data.projects.add(project)
        }
        save()
    }

    suspend fun deleteProject(id: Int) {
        val index = data.projects.indexOfFirst { it.id == id }
        if (index != -1) {
            val project = data.projects[index]
            data.projects[index] = project.copy(
                deleted = true,
                modified = getCurrentTimestamp()
            )

            // Also mark related assignments as deleted
            val timestamp = getCurrentTimestamp()
            val assignmentsCount = data.assignments.count { it.projectId == id && it.deleted != true }
            data.assignments.forEachIndexed { idx, assignment ->
                if (assignment.projectId == id && assignment.deleted != true) {
                    data.assignments[idx] = assignment.copy(
                        deleted = true,
                        lastModified = timestamp
                    )
                }
            }
            Logger.log(LogLevel.INFO, "Marked project as deleted: id=$id, name=${project.name}, marked $assignmentsCount assignments as deleted")
            save()
        }
    }

    fun getPatternById(id: Int): Pattern? = data.patterns.firstOrNull { it.id == id && it.deleted != true }

    fun createNewPattern(): Pattern {
        val existingIds = data.patterns.map { it.id }.toSet()
        var newId: Int
        do {
            newId = Random.nextInt(1_000_000, 10_000_000)
        } while (existingIds.contains(newId))
        return Pattern(
            id = newId,
            name = "Pattern#$newId"
        ).also {
            GlobalScope.launch {
                Logger.log(LogLevel.INFO, "Created new pattern: id=${it.id}, name=${it.name}")
            }
        }
    }

    suspend fun addOrUpdatePattern(pattern: Pattern) {
        val index = data.patterns.indexOfFirst { it.id == pattern.id }
        if (index != -1) {
            Logger.log(LogLevel.INFO, "Updated pattern: id=${pattern.id}, name=${pattern.name}")
            data.patterns[index] = pattern
        } else {
            Logger.log(LogLevel.INFO, "Added pattern: id=${pattern.id}, name=${pattern.name}")
            data.patterns.add(pattern)
        }
        save()
    }

    suspend fun deletePattern(id: Int) {
        val index = data.patterns.indexOfFirst { it.id == id }
        if (index != -1) {
            val pattern = data.patterns[index]
            data.patterns[index] = pattern.copy(
                deleted = true,
                modified = getCurrentTimestamp()
            )

            // Unlink pattern from projects (but don't delete projects)
            val affectedProjects = data.projects.count { it.patternId == id && it.deleted != true }
            data.projects.forEachIndexed { idx, project ->
                if (project.patternId == id && project.deleted != true) {
                    data.projects[idx] = project.copy(
                        patternId = null,
                        modified = getCurrentTimestamp()
                    )
                }
            }
            Logger.log(LogLevel.INFO, "Marked pattern as deleted: id=$id, name=${pattern.name}, unlinked from $affectedProjects projects")
            save()
        }
    }

    suspend fun updatePatternPdfId(patternId: Int, pdfId: Int?) {
        val index = data.patterns.indexOfFirst { it.id == patternId }
        if (index != -1) {
            data.patterns[index] = data.patterns[index].copy(pdfId = pdfId)
            Logger.log(LogLevel.INFO, "Updated pattern PDF: patternId=$patternId, pdfId=$pdfId")
        }
        save()
    }

    fun availableForYarn(yarnId: Int, forProjectId: Int? = null): Int {
        val yarn = getYarnById(yarnId) ?: return 0
        val used = data.assignments
            .filter { it.yarnId == yarnId && it.projectId != forProjectId }
            .sumOf { it.amount }
        return yarn.amount - used
    }

    suspend fun setProjectAssignments(projectId: Int, assignments: Map<Int, Int>) {
        // Remove existing assignments for this project
        val removedCount = data.assignments.count { it.projectId == projectId }
        data.assignments.removeAll { it.projectId == projectId }
        Logger.log(LogLevel.INFO, "Removed $removedCount existing assignments for projectId=$projectId")

        // Add new assignments with generated IDs and lastModified timestamp
        val timestamp = getCurrentTimestamp()
        val existingIds = data.assignments.map { it.id }.toSet()
        var addedCount = 0
        for ((yarnId, amount) in assignments) {
            if (amount > 0) {
                var newId: Int
                do {
                    newId = Random.nextInt(until = 2_000_000_000)
                } while (existingIds.contains(newId))
                val assignment = Assignment(
                    id = newId,
                    yarnId = yarnId,
                    projectId = projectId,
                    amount = amount,
                    lastModified = timestamp
                )
                data.assignments.add(assignment)
                Logger.log(LogLevel.INFO, "Updated assignment: id=${assignment.id}, yarnId=$yarnId, projectId=$projectId, amount=$amount")
                addedCount++
            }
        }
        Logger.log(LogLevel.INFO, "Added $addedCount assignments for projectId=$projectId")
        save()
    }
}
