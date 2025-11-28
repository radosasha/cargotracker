package com.shiplocate.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIPasteboard

/**
 * iOS реализация для работы с буфером обмена
 */
actual class ClipboardManager {
    actual fun copyToClipboard(text: String) {
        UIPasteboard.generalPasteboard.setString(text)
    }
}

@Composable
actual fun rememberClipboardManager(): ClipboardManager {
    return remember { ClipboardManager() }
}

