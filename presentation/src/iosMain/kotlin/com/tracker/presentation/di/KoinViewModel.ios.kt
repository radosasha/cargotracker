package com.tracker.presentation.di

import androidx.compose.runtime.Composable
import com.tracker.presentation.feature.home.HomeViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * iOS реализация для инъекции ViewModels через Koin
 */
@Composable
actual fun koinHomeViewModel(): HomeViewModel {
    return object : KoinComponent {
        val viewModel: HomeViewModel by inject()
    }.viewModel
}
