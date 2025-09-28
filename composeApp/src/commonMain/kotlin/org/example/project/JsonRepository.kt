package org.example.project

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.NoSuchElementException

class JsonRepository(private val fileHandler: FileHandler) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private var cache: AppData = AppData()

    // ---------- On-disk schema (v2) ----------
    // usages as flat map: "yarnId,projectId" -> amount
    @Serializable
    private data class DiskV2(
        val yarns: List<Yarn> = emptyList(),
        val projects: List<Project> = emptyList(),
        val usages: Map<String, Int> = emptyMap()
    )

    // ---------- Load ----------
    suspend fun load(): AppData {
        val text = fileHandler.readFile()
        cache = if (text.isNotBlank()) {
            val disk = json.decodeFromString<DiskV2>(text)
            AppData(
                yarns = disk.yarns,
                projects = disk.projects,
                usages = disk.usages.mapNotNull { (key, amount) ->
                    parseKey(key)?.let { (yarnId, projectId) ->
                        Usage(projectId = projectId, yarnId = yarnId, amount = amount)
                    }
                }
            )
        } else {
            AppData()
        }
        return cache
    }

    // ---------- Save ----------
    private suspend fun save(data: AppData): AppData {
        cache = data
        // Build flat map; last write wins if duplicates are present in memory.
        val flatUsages: Map<String, Int> = buildMap {
            data.usages.forEach { u ->
                put("${u.yarnId},${u.projectId}", u.amount)
            }
        }
        val disk = DiskV2(
            yarns = data.yarns,
            projects = data.projects,
            usages = flatUsages
        )
        fileHandler.writeFile(json.encodeToString<DiskV2>(disk))
        return cache
    }

    // =========================================================
    // YARN
    // =========================================================
    suspend fun addOrUpdateYarn(y: Yarn): AppData {
        val exists = cache.yarns.any { it.id == y.id }
        val newList = if (exists) {
            cache.yarns.map { if (it.id == y.id) y else it }
        } else cache.yarns + y
        return save(cache.copy(yarns = newList))
    }

    suspend fun deleteYarn(id: Int): AppData {
        val newYarns = cache.yarns.filterNot { it.id == id }
        val newUsages = cache.usages.filterNot { it.yarnId == id }
        return save(cache.copy(yarns = newYarns, usages = newUsages))
    }

    fun getYarnById(id: Int): Yarn =
        cache.yarns.firstOrNull { it.id == id } ?: throw NoSuchElementException("Yarn with id $id not found")

    fun nextYarnId(): Int = (cache.yarns.maxOfOrNull { it.id } ?: 0) + 1

    // =========================================================
    // PROJECT
    // =========================================================
    suspend fun addOrUpdateProject(p: Project): AppData {
        val exists = cache.projects.any { it.id == p.id }
        val newList = if (exists) {
            cache.projects.map { if (it.id == p.id) p else it }
        } else cache.projects + p
        return save(cache.copy(projects = newList))
    }

    suspend fun deleteProject(id: Int): AppData {
        val newProjects = cache.projects.filterNot { it.id == id }
        val newUsages = cache.usages.filterNot { it.projectId == id }
        return save(cache.copy(projects = newProjects, usages = newUsages))
    }

    fun getProjectById(id: Int): Project =
        cache.projects.firstOrNull { it.id == id } ?: throw NoSuchElementException("Project with id $id not found")

    fun nextProjectId(): Int = (cache.projects.maxOfOrNull { it.id } ?: 0) + 1

    // =========================================================
    // USAGE (unique per (projectId, yarnId) logically)
    // =========================================================
    suspend fun setProjectAssignments(projectId: Int, assignments: Map<Int, Int>): AppData {
        val remaining = cache.usages.filterNot { it.projectId == projectId }
        val newOnes = assignments
            .filterValues { it > 0 }
            .map { (yarnId, amount) -> Usage(projectId, yarnId, amount) }
        return save(cache.copy(usages = remaining + newOnes))
    }

    suspend fun upsertUsage(u: Usage): AppData {
        val without = cache.usages.filterNot { it.projectId == u.projectId && it.yarnId == u.yarnId }
        return save(cache.copy(usages = without + u))
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

    // parse "yarnId,projectId" -> Pair<yarnId, projectId>
    private fun parseKey(key: String): Pair<Int, Int>? {
        val parts = key.split(',').map { it.trim() }
        if (parts.size != 2) return null
        val yarnId = parts[0].toIntOrNull() ?: return null
        val projectId = parts[1].toIntOrNull() ?: return null
        return yarnId to projectId
    }
}