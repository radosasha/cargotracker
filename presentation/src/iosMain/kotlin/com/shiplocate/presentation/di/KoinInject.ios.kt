package com.shiplocate.presentation.di

import androidx.compose.runtime.Composable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get as koinGet

/**
 * iOS реализация инъекции зависимостей через Koin
 */
@Composable
actual inline fun <reified T> koinInject(): T {
    return KoinHelper.get()
}

object KoinHelper : KoinComponent {
    inline fun <reified T> get(): T {
        return koinGet()
    }
}
