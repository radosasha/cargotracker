package com.tracker.presentation.di

import androidx.compose.runtime.Composable

/**
 * Expect/Actual для инъекции зависимостей через Koin
 */
@Composable
expect inline fun <reified T> koinInject(): T


