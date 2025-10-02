package com.tracker.di

import androidx.compose.runtime.Composable
import com.tracker.presentation.feature.home.HomeViewModel

/**
 * Expect/Actual для инъекции зависимостей через Koin
 */
@Composable
expect fun koinInjectHomeViewModel(): HomeViewModel
