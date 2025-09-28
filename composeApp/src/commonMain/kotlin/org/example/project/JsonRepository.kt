package org.example.project

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class JsonRepository(private val fileHandler: FileHandler) {
    private val json = Json { prettyPrint = true }
    private var cache: AppData = AppData()

    // ---------- Load ----------
    suspend fun load(): AppData {
        val text = fileHandler.readFile()
        cache = if (text.isNotBlank()) json.decodeFromString(text) else AppData()
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

    fun getYarnById(id: Int): Yarn? = cache.yarns.firstOrNull { it.id == id }
    fun nextYarnId(): Int = (cache.yarns.maxOfOrNull { it.id } ?: 0) + 1

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

    fun getProjectById(id: Int): Project? = cache.projects.firstOrNull { it.id == id }
    fun nextProjectId(): Int = (cache.projects.maxOfOrNull { it.id } ?: 0) + 1

    // =========================================================
    // USAGE (Link Project ↔ Yarn)
    // =========================================================
    suspend fun upsertUsage(u: Usage): AppData {
        val idx = cache.usages.indexOfFirst { it.projectId == u.projectId && it.yarnId == u.yarnId }
        val newUsages = if (idx >= 0) {
            cache.usages.toMutableList().also { it[idx] = u }
        } else {
            cache.usages + u
        }
        return save(cache.copy(usages = newUsages))
    }

    suspend fun removeUsage(projectId: Int, yarnId: Int): AppData {
        val newUsages = cache.usages.filterNot { it.projectId == projectId && it.yarnId == yarnId }
        return save(cache.copy(usages = newUsages))
    }
}
