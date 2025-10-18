package com.shiplocate.presentation.navigation

import androidx.navigation.NavBackStackEntry

/**
 * Android реализация получения аргументов из навигации
 */
actual fun NavBackStackEntry.getStringArgument(key: String): String? {
    return arguments?.getString(key)
}
