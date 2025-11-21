package com.shiplocate.presentation.util

import androidx.compose.runtime.Composable

/**
 * Expect класс для открытия email клиента
 */
expect class EmailLauncher {
    fun openEmail(email: String)
}

/**
 * Expect функция для создания EmailLauncher
 */
@Composable
expect fun rememberEmailLauncher(): EmailLauncher

