package org.example.project

import android.content.Intent
import android.net.Uri

actual fun sendEmail(address: String, subject: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }
    val chooser = Intent.createChooser(intent, "Email")
    chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    CONTEXT.startActivity(chooser)
}
