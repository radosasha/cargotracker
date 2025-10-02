package com.tracker.presentation.di

import androidx.compose.runtime.Composable
import com.tracker.presentation.feature.home.HomeViewModel

/**
 * Expect/Actual для инъекции ViewModels через Koin
 */
@Composable
expect fun koinHomeViewModel(): HomeViewModel
