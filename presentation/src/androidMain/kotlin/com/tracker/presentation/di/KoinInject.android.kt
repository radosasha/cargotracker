package com.tracker.presentation.di

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject as koinInjectAndroid

/**
 * Android реализация инъекции зависимостей через Koin
 */
@Composable
actual inline fun <reified T> koinInject(): T {
    return koinInjectAndroid()
}


