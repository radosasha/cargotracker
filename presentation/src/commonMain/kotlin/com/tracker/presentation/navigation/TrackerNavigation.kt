package com.tracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tracker.presentation.di.koinHomeViewModel
import com.tracker.presentation.feature.home.HomeScreen

/**
 * Правильная навигация с NavHostController и NavHost
 * ViewModel создается прямо в composable блоке
 */
@Composable
fun TrackerNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.HOME
    ) {
        composable(Screen.HOME) {
            val viewModel = koinHomeViewModel()
            HomeScreen(viewModel = viewModel)
        }
        
        // В будущем здесь будут другие экраны
        // composable(Screen.SETTINGS) {
        //     val viewModel = koinViewModel()
        //     SettingsScreen(viewModel = viewModel)
        // }
        
        // composable(Screen.ABOUT) {
        //     val viewModel = koinViewModel()
        //     AboutScreen(viewModel = viewModel)
        // }
    }
}
