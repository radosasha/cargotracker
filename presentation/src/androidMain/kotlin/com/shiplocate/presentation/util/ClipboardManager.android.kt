package com.shiplocate.presentation.util

import android.content.ClipData
import android.content.ClipboardManager as AndroidClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Android реализация для работы с буфером обмена
 */
actual class ClipboardManager(private val context: Context) {
    actual fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as AndroidClipboardManager
        val clip = ClipData.newPlainText("Message", text)
        clipboard.setPrimaryClip(clip)
    }
}

@Composable
actual fun rememberClipboardManager(): ClipboardManager {
    val context = LocalContext.current
    return remember(context) { ClipboardManager(context) }
}

