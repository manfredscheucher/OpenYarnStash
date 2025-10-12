package org.example.project

import android.content.Intent
import android.net.Uri

actual fun openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    val chooser = Intent.createChooser(intent, "Open URL with")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    CONTEXT.startActivity(chooser)
}
