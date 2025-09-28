package org.example.project

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.NoSuchElementException // Ensure this import is present

class JsonRepository(private val fileHandler: FileHandler) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    private var cache: AppData = AppData()

    // ---------- Load (with normalize to enforce uniqueness) ----------
    suspend fun load(): AppData {
        val text = fileHandler.readFile()
        cache = if (text.isNotBlank()) {
            val parsed = json.decodeFromString<AppData>(text)
            parsed.normalize()
        } else AppData()
        return cache
    }

    // ---------- Save ----------
    private suspend fun save(data: AppData): AppData {
        cache = data
        fileHandler.writeFile(json.encodeToString(data))
        return cache
    }

    // =========================================================
    // YARN
    // =========================================================
    suspend fun addOrUpdateYarn(y: Yarn): AppData {
        val exists = cache.yarns.any { it.id == y.id }
        val newList = if (exists) {
            cache.yarns.map { if (it.id == y.id) y else it }
        } else {
            cache.yarns + y
        }
        return save(cache.copy(yarns = newList))
    }

    suspend fun deleteYarn(id: Int): AppData {
        val newYarns = cache.yarns.filterNot { it.id == id }
        val newUsages = cache.usages.filterNot { it.yarnId == id }
        return save(cache.copy(yarns = newYarns, usages = newUsages))
    }

    fun getYarnById(id: Int): Yarn = 
        cache.yarns.firstOrNull { it.id == id } ?: throw NoSuchElementException("Yarn with id $id not found")

    fun nextYarnId(): Int = if (cache.yarns.isEmpty()) 1 else (cache.yarns.maxOf { it.id }) + 1

    // =========================================================
    // PROJECT
    // =========================================================
    suspend fun addOrUpdateProject(p: Project): AppData {
        val exists = cache.projects.any { it.id == p.id }
        val newList = if (exists) {
            cache.projects.map { if (it.id == p.id) p else it }
        } else {
            cache.projects + p
        }
        return save(cache.copy(projects = newList))
    }

    suspend fun deleteProject(id: Int): AppData {
        val newProjects = cache.projects.filterNot { it.id == id }
        val newUsages = cache.usages.filterNot { it.projectId == id }
        return save(cache.copy(projects = newProjects, usages = newUsages))
    }

    fun getProjectById(id: Int): Project = 
        cache.projects.firstOrNull { it.id == id } ?: throw NoSuchElementException("Project with id $id not found")

    fun nextProjectId(): Int = if (cache.projects.isEmpty()) 1 else (cache.projects.maxOf { it.id }) + 1

    // =========================================================
    // USAGE (unique per (projectId, yarnId))
    // =========================================================

    /** Replace all assignments for a project atomically. */
    suspend fun setProjectAssignments(projectId: Int, assignments: Map<Int, Int>): AppData {
        val remaining = cache.usages.filterNot { it.projectId == projectId }
        val newOnes = assignments
            .filterValues { it > 0 }
            .map { (yarnId, amount) -> Usage(projectId, yarnId, amount) }
        return save(cache.copy(usages = remaining + newOnes))
    }

    /** Upsert a single usage; replaces any existing pair. */
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

    /**
     * Available amount for a yarn. If [forProjectId] is set, the current assignment
     * in that project is "given back" so editing doesn't block you.
     * Throws NoSuchElementException if yarnId does not exist.
     */
    fun availableForYarn(yarnId: Int, forProjectId: Int? = null): Int {
        val yarn = getYarnById(yarnId) // This will throw if yarn not found
        val totalAssigned = assignedForYarn(yarnId)
        val currentInThisProject = forProjectId?.let { assignedForYarnInProject(yarnId, it) } ?: 0
        return (yarn.amount - (totalAssigned - currentInThisProject)).coerceAtLeast(0)
    }
}

// ---------- Internal: normalize duplicates after reading ----------
private fun AppData.normalize(): AppData {
    // Keep only one Usage per (projectId, yarnId). Last one wins.
    val deduped = usages
        .asReversed()
        .associateBy { it.projectId to it.yarnId }
        .values
        .toList()
        .asReversed()
    return copy(usages = deduped)
}
