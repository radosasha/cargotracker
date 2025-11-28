package com.shiplocate.presentation.util

import androidx.compose.runtime.Composable

/**
 * Expect класс для работы с буфером обмена
 */
expect class ClipboardManager {
    fun copyToClipboard(text: String)
}

/**
 * Expect функция для создания ClipboardManager
 */
@Composable
expect fun rememberClipboardManager(): ClipboardManager

