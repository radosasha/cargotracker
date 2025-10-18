package com.shiplocate.di

import androidx.compose.runtime.Composable
import com.shiplocate.presentation.feature.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
actual fun koinInjectHomeViewModel(): HomeViewModel {
    return koinViewModel()
}
