package org.example.project

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class JsonSettingsManager(private val fileHandler: FileHandler, private val filePath: String) {

    private var settings: Settings = Settings()

    suspend fun loadSettings(): Settings {
        val content = fileHandler.readText(filePath)
        settings = if (content.isNotEmpty()) {
            try {
                Json.decodeFromString<Settings>(content)
            } catch (e: SerializationException) {
                Logger.log(LogLevel.ERROR, "Error decoding settings JSON: ${e.message}", e)
                println("Error decoding settings JSON: ${e.message}")
                Settings()
            } catch (e: Exception) {
                Logger.log(LogLevel.ERROR, "An unexpected error occurred while loading settings: ${e.message}", e)
                println("An unexpected error occurred while loading settings: ${e.message}")
                Settings()
            }
        } else {
            Settings()
        }
        return settings
    }

    suspend fun saveSettings(settings: Settings) {
        this.settings = settings
        val content = Json.encodeToString(this@JsonSettingsManager.settings)
        fileHandler.writeText(filePath, content)
    }
}
