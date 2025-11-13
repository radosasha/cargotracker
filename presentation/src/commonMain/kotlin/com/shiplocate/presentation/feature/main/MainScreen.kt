package com.shiplocate.presentation.feature.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.shiplocate.presentation.component.LoadsBottomNavigationBar
import com.shiplocate.presentation.component.TrackerTopAppBar
import com.shiplocate.presentation.di.koinLoadsViewModel
import com.shiplocate.presentation.navigation.Screen
import com.shiplocate.presentation.navigation.TrackerNavigation

/**
 * Главный экран приложения с навигацией
 * Единый Scaffold для всех экранов с динамическим TopAppBar
 */
@Suppress("FunctionName")
@Composable
fun MainScreen() {
    var navController by remember { mutableStateOf<NavController?>(null) }
    var currentRoute by remember { mutableStateOf<String?>(null) }

    MaterialTheme {
        // Remember LoadsViewModel only when on LOADS screen
        val loadsViewModel = remember(currentRoute) {
            if (currentRoute == Screen.LOADS) {
                koinLoadsViewModel()
            } else {
                null
            }
        }
        val currentPage by loadsViewModel?.currentPage?.collectAsStateWithLifecycle() 
            ?: remember { mutableStateOf(0) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                navController?.let { controller ->
                    TrackerTopAppBar(
                        navController = controller,
                        currentRoute = currentRoute,
                        // Long press на Loads экране для перехода к логам
                        onLongPressTitle =
                            if (currentRoute == Screen.LOADS) {
                                { controller.navigate(Screen.LOGS) }
                            } else {
                                null
                            },
                    )
                }
            },
            bottomBar = {
                if (currentRoute == Screen.LOADS && loadsViewModel != null) {
                    LoadsBottomNavigationBar(
                        currentPage = currentPage,
                        onPageSelected = { page ->
                            loadsViewModel.setCurrentPage(page)
                        },
                    )
                }
            },
        ) { paddingValues ->
            TrackerNavigation(
                paddingValues = paddingValues,
                onNavControllerReady = { controller, route ->
                    navController = controller
                    currentRoute = route
                },
            )
        }
    }
}
