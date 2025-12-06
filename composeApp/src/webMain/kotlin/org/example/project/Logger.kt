package org.example.project

actual fun initializeLogger(fileHandler: FileHandler, settings: Settings) {
    Logger.init(fileHandler, settings)
}
