package com.tracker

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.tracker.presentation.feature.main.MainScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
@Suppress("ktlint:standard:function-naming")
fun App() {
    MaterialTheme {
        TrackerApp()
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun TrackerApp() {
    // Используем MainScreen с навигацией
    MainScreen()
}
