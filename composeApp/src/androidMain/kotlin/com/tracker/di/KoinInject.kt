package com.tracker.di

import androidx.compose.runtime.Composable
import com.tracker.presentation.feature.home.HomeViewModel
import org.koin.compose.koinInject

/**
 * Android реализация для инъекции зависимостей через Koin
 */
@Composable
actual fun koinInjectHomeViewModel(): HomeViewModel = koinInject()
