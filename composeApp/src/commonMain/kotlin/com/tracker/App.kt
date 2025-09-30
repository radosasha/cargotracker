package com.tracker

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.tracker.presentation.feature.home.HomeScreen
import com.tracker.presentation.feature.home.HomeViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    MaterialTheme {
        TrackerApp()
    }
}

@Composable
fun TrackerApp() {
    // Получаем HomeViewModel через Koin
    val viewModel: HomeViewModel = koinInject()
    
    // Используем HomeScreen из presentation слоя
    HomeScreen(viewModel = viewModel)
}