package org.example.project

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun sendEmail(address: String, subject: String) {
    val encodedSubject = subject.replace(" ", "%20")
    val urlString = "mailto:$address?subject=$encodedSubject"
    NSURL.URLWithString(urlString)?.let { url ->
        if (UIApplication.sharedApplication.canOpenURL(url)) {
            UIApplication.sharedApplication.openURL(url)
        }
    }
}
