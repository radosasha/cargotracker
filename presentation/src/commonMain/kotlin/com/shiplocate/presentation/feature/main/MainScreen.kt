package com.shiplocate.presentation.feature.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shiplocate.presentation.navigation.TrackerNavigation

/**
 * Главный экран приложения с навигацией
 */
@Suppress("FunctionName")
@Composable
fun MainScreen() {
    MaterialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { paddingValues ->
            TrackerNavigation()
        }
    }
}
