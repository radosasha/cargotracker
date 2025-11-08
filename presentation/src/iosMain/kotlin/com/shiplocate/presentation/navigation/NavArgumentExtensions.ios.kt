package com.shiplocate.presentation.navigation

import androidx.navigation.NavBackStackEntry

/**
 * iOS реализация получения аргументов из навигации
 * В iOS Navigation Compose использует SavedStateHandle для аргументов
 */
actual fun NavBackStackEntry.getStringArgument(key: String): String? {
    return savedStateHandle.get<String>(key)
}

actual fun NavBackStackEntry.getLongArgument(key: String): Long? {
    return savedStateHandle.get<Long>(key)
}
