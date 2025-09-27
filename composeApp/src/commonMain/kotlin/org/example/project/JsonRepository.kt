package org.example.project

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class JsonRepository(private val fileHandler: FileHandler) {
    private val json = Json { prettyPrint = true }
    private var cache: AppData = AppData()

    suspend fun load(): AppData {
        val text = fileHandler.readFile()
        cache = if (text.isNotBlank()) json.decodeFromString(text) else AppData()
        return cache
    }

    // ---------- Yarn ----------
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
        // (3) später: usages mit diesem yarnId auch entfernen
        val newList = cache.yarns.filterNot { it.id == id }
        val newUsages = cache.usages.filterNot { it.yarnId == id }
        return save(cache.copy(yarns = newList, usages = newUsages))
    }

    fun getYarnById(id: Int): Yarn? = cache.yarns.firstOrNull { it.id == id }

    fun nextYarnId(): Int = (cache.yarns.maxOfOrNull { it.id } ?: 0) + 1

    // ---------- Save ----------
    private suspend fun save(data: AppData): AppData {
        cache = data
        fileHandler.writeFile(json.encodeToString(data))
        return cache
    }
}
