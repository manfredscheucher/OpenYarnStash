package org.example.project

import android.os.Build
import java.io.File

actual fun isDeviceRooted(): Boolean {
    // Check 1: su binary in common locations
    val suPaths = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/data/local/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/dev/com.koushikdutta.superuser.daemon/"
    )
    if (suPaths.any { File(it).exists() }) return true

    // Check 2: known root management apps
    val rootApps = arrayOf(
        "com.noshufou.android.su",
        "com.thirdparty.superuser",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.zachspong.temprootremovejb",
        "com.ramdroid.appquarantine",
        "com.topjohnwu.magisk"
    )
    val pm = try { getContext()?.let { (it as android.content.Context).packageManager } } catch (_: Exception) { null }
    if (pm != null) {
        for (pkg in rootApps) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (_: Exception) { }
        }
    }

    // Check 3: build tags (test-keys instead of release-keys)
    if (Build.TAGS?.contains("test-keys") == true) return true

    // Check 4: writable /system
    try {
        val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
        if (process.inputStream.read() != -1) return true
    } catch (_: Exception) { }

    return false
}
