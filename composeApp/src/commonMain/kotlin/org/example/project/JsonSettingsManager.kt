package org.example.project

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JsonSettingsManager(private val fileHandler: FileHandler, private val filePath: String) {

    private var settings: SettingsData = SettingsData()

    suspend fun loadSettings(): SettingsData {
        val content = fileHandler.readFile(filePath)
        settings = if (content.isNotEmpty()) {
            try {
                Json.decodeFromString<SettingsData>(content)
            } catch (e: SerializationException) {
                println("Error decoding settings JSON: ${e.message}")
                SettingsData()
            } catch (e: Exception) {
                println("An unexpected error occurred while loading settings: ${e.message}")
                SettingsData()
            }
        } else {
            SettingsData()
        }
        return settings
    }

    suspend fun saveSettings(settingsData: SettingsData) {
        this.settings = settingsData
        val content = Json.encodeToString(settings)
        fileHandler.writeFile(filePath, content)
    }
}
