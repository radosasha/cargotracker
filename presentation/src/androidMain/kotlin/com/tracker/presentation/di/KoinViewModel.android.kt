package com.tracker.presentation.di

import androidx.compose.runtime.Composable
import com.tracker.presentation.feature.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Android реализация для инъекции ViewModels через Koin
 */
@Composable
actual fun koinHomeViewModel(): HomeViewModel = koinViewModel()
