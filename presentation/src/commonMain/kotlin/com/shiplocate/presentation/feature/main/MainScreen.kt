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
import androidx.navigation.NavController
import com.shiplocate.presentation.component.LoadsBottomNavigationBar
import com.shiplocate.presentation.component.TrackerTopAppBar
import com.shiplocate.presentation.model.BottomBarState
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
    var logoutCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    MaterialTheme {
        // Состояние bottomBar, управляемое через callback из TrackerNavigation
        var bottomBarState by remember { mutableStateOf<BottomBarState>(BottomBarState.None) }

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
                        // Logout callback для LoadsScreen
                        onLogoutClick = logoutCallback,
                    )
                }
            },
            bottomBar = {
                when (val state = bottomBarState) {
                    is BottomBarState.Loads -> {
                        LoadsBottomNavigationBar(
                            currentPage = state.currentPage,
                            onPageSelected = state.onPageSelected,
                        )
                    }
                    is BottomBarState.None -> {
                        // No bottom bar
                    }
                }
            },
        ) { paddingValues ->
            TrackerNavigation(
                paddingValues = paddingValues,
                onNavControllerReady = { controller, route ->
                    navController = controller
                    currentRoute = route
                },
                onBottomBarStateChanged = { state ->
                    bottomBarState = state
                },
                onLogoutCallbackReady = { callback ->
                    logoutCallback = callback
                },
            )
        }
    }
}
