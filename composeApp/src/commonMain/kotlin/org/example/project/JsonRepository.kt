package org.example.project

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.NoSuchElementException
import kotlin.random.Random

class JsonRepository(private val fileHandler: FileHandler) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private var cache: AppData = AppData()

    // ---------- Load ----------
    suspend fun load(): AppData {
        val text = fileHandler.readFile()
        cache = if (text.isNotBlank()) {
            try {
                json.decodeFromString<AppData>(text)
            } catch (e: Exception) {
                // If decoding fails, start with empty data to avoid a crash.
                AppData()
            }
        } else {
            AppData()
        }
        return cache
    }

    // ---------- Save ----------
    private suspend fun save(data: AppData): AppData {
        cache = data
        // Save directly in the new AppData format
        fileHandler.writeFile(json.encodeToString(data))
        return cache
    }

    // =========================================================
    // YARN
    // =========================================================
    suspend fun addOrUpdateYarn(y: Yarn): AppData {
        val exists = cache.yarns.any { it.id == y.id }
        val yarnToAdd = if (exists) y else y.copy(dateAdded = getCurrentTimestamp())
        val newList = if (exists) {
            cache.yarns.map { if (it.id == y.id) yarnToAdd else it }
        } else cache.yarns + yarnToAdd
        return save(cache.copy(yarns = newList))
    }

    suspend fun deleteYarn(id: Int): AppData {
        val newYarns = cache.yarns.filterNot { it.id == id }
        val newUsages = cache.usages.filterNot { it.yarnId == id }
        return save(cache.copy(yarns = newYarns, usages = newUsages))
    }

    fun getYarnById(id: Int): Yarn =
        cache.yarns.firstOrNull { it.id == id } ?: throw NoSuchElementException("Yarn with id $id not found")

    fun nextYarnId(): Int {
        var newId: Int
        do {
            newId = Random.nextInt(1, Int.MAX_VALUE)
        } while (cache.yarns.any { it.id == newId })
        return newId
    }

    // =========================================================
    // PROJECT
    // =========================================================
    suspend fun addOrUpdateProject(p: Project): AppData {
        val exists = cache.projects.any { it.id == p.id }
        val projectToAdd = if (exists) p else p.copy(dateAdded = getCurrentTimestamp())
        val newList = if (exists) {
            cache.projects.map { if (it.id == p.id) projectToAdd else it }
        } else cache.projects + projectToAdd
        return save(cache.copy(projects = newList))
    }

    suspend fun deleteProject(id: Int): AppData {
        val newProjects = cache.projects.filterNot { it.id == id }
        val newUsages = cache.usages.filterNot { it.projectId == id }
        return save(cache.copy(projects = newProjects, usages = newUsages))
    }

    fun getProjectById(id: Int): Project =
        cache.projects.firstOrNull { it.id == id } ?: throw NoSuchElementException("Project with id $id not found")

    fun nextProjectId(): Int {
        var newId: Int
        do {
            newId = Random.nextInt(1, Int.MAX_VALUE)
        } while (cache.projects.any { it.id == newId })
        return newId
    }

    // =========================================================
    // USAGE (unique per (projectId, yarnId) logically)
    // =========================================================
    suspend fun setProjectAssignments(projectId: Int, assignments: Map<Int, Int>): AppData {
        val remaining = cache.usages.filterNot { it.projectId == projectId }
        val newOnes = assignments
            .filterValues { it > 0 }
            .map { (yarnId, amount) -> Usage(projectId, yarnId, amount, dateAdded = getCurrentTimestamp()) }
        return save(cache.copy(usages = remaining + newOnes))
    }

    suspend fun upsertUsage(u: Usage): AppData {
        val without = cache.usages.filterNot { it.projectId == u.projectId && it.yarnId == u.yarnId }
        return save(cache.copy(usages = without + u.copy(dateAdded = getCurrentTimestamp())))
    }

    suspend fun removeUsage(projectId: Int, yarnId: Int): AppData {
        val newUsages = cache.usages.filterNot { it.projectId == projectId && it.yarnId == yarnId }
        return save(cache.copy(usages = newUsages))
    }

    // ---------- Queries / helpers ----------
    fun usagesForProject(projectId: Int): List<Usage> =
        cache.usages.filter { it.projectId == projectId }

    fun usagesForYarn(yarnId: Int): List<Usage> =
        cache.usages.filter { it.yarnId == yarnId }

    fun assignedForYarn(yarnId: Int): Int =
        usagesForYarn(yarnId).sumOf { it.amount }

    fun assignedForYarnInProject(yarnId: Int, projectId: Int): Int =
        cache.usages.firstOrNull { it.yarnId == yarnId && it.projectId == projectId }?.amount ?: 0

    fun availableForYarn(yarnId: Int, forProjectId: Int? = null): Int {
        val yarn = getYarnById(yarnId)
        val totalAssigned = assignedForYarn(yarnId)
        val currentInThisProject = forProjectId?.let { assignedForYarnInProject(yarnId, it) } ?: 0
        return (yarn.amount - (totalAssigned - currentInThisProject)).coerceAtLeast(0)
    }
}