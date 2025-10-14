package com.tracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tracker.domain.usecase.auth.HasAuthSessionUseCase
import com.tracker.presentation.di.koinEnterPhoneViewModel
import com.tracker.presentation.di.koinEnterPinViewModel
import com.tracker.presentation.di.koinHomeViewModel
import com.tracker.presentation.di.koinInject
import com.tracker.presentation.di.koinLoadsViewModel
import com.tracker.presentation.feature.auth.EnterPhoneScreen
import com.tracker.presentation.feature.auth.EnterPinScreen
import com.tracker.presentation.feature.home.HomeScreen
import com.tracker.presentation.feature.loads.LoadsScreen
import kotlinx.coroutines.launch

/**
 * Навигация с использованием строковых маршрутов
 * (Type-safe args не поддерживаются в KMP)
 * 
 * Проверяет наличие токена при старте
 */
@Composable
fun TrackerNavigation() {
    val navController = rememberNavController()
    val hasAuthSessionUseCase: HasAuthSessionUseCase = koinInject()
    val scope = rememberCoroutineScope()
    
    var isCheckingAuth by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf(Screen.ENTER_PHONE) }
    
    // Check auth session on start
    LaunchedEffect(Unit) {
        scope.launch {
            val hasSession = hasAuthSessionUseCase()
            startDestination = if (hasSession) Screen.LOADS else Screen.ENTER_PHONE
            isCheckingAuth = false
        }
    }
    
    if (isCheckingAuth) {
        // Show loading or splash screen
        return
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
        composable(Screen.ENTER_PHONE) {
            val viewModel = koinEnterPhoneViewModel()
            EnterPhoneScreen(
                onNavigateToPin = { phone ->
                    navController.navigate(Screen.enterPin(phone))
                },
                viewModel = viewModel
            )
        }
        
        composable(
            route = Screen.ENTER_PIN,
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phone = backStackEntry.getStringArgument("phone") ?: ""
            val viewModel = koinEnterPinViewModel()
            
            EnterPinScreen(
                phone = phone,
                onNavigateToHome = {
                    // Clear back stack and navigate to loads
                    navController.navigate(Screen.LOADS) {
                        popUpTo(Screen.ENTER_PHONE) { inclusive = true }
                    }
                },
                onNavigateBack = { errorMessage ->
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }
        
        // Main app screens
        composable(Screen.LOADS) {
            val viewModel = koinLoadsViewModel()
            LoadsScreen(viewModel = viewModel)
        }
        
        composable(Screen.HOME) {
            val viewModel = koinHomeViewModel()
            HomeScreen(viewModel = viewModel)
        }
    }
}
