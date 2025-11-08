package com.shiplocate.presentation.navigation

import androidx.navigation.NavBackStackEntry

/**
 * Expect/Actual для получения аргументов из навигации
 */
expect fun NavBackStackEntry.getStringArgument(key: String): String?
expect fun NavBackStackEntry.getLongArgument(key: String): Long?
