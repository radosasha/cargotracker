package com.shiplocate.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIApplication
import platform.Foundation.NSURL

/**
 * iOS реализация для открытия email клиента
 */
actual class EmailLauncher {
    actual fun openEmail(email: String) {
        val urlString = "mailto:$email"
        val url = NSURL.URLWithString(urlString)
        url?.let {
            UIApplication.sharedApplication.openURL(it)
        }
    }
}

@Composable
actual fun rememberEmailLauncher(): EmailLauncher {
    return remember { EmailLauncher() }
}

