package org.example.project

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val fileHandler: FileHandler = IosFileHandler()
}

actual fun getPlatform(context: Any?): Platform = IOSPlatform()