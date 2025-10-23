package com.shiplocate.di

import androidx.compose.runtime.Composable
import com.shiplocate.presentation.feature.home.HomeViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Composable
actual fun koinInjectHomeViewModel(): HomeViewModel {
    return object : KoinComponent {
        val viewModel: HomeViewModel by inject()
    }.viewModel
}
