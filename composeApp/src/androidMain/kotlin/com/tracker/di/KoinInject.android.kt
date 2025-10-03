package com.tracker.di

import androidx.compose.runtime.Composable
import com.tracker.presentation.feature.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
actual fun koinInjectHomeViewModel(): HomeViewModel {
    return koinViewModel()
}