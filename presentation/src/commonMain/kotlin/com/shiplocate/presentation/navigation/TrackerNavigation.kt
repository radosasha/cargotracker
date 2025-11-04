package com.shiplocate.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shiplocate.domain.usecase.auth.HasAuthSessionUseCase
import com.shiplocate.presentation.di.koinEnterPhoneViewModel
import com.shiplocate.presentation.di.koinEnterPinViewModel
import com.shiplocate.presentation.di.koinHomeViewModel
import com.shiplocate.presentation.di.koinInject
import com.shiplocate.presentation.di.koinLoadsViewModel
import com.shiplocate.presentation.di.koinLogsViewModel
import com.shiplocate.presentation.feature.auth.EnterPhoneScreen
import com.shiplocate.presentation.feature.auth.EnterPhoneViewModel
import com.shiplocate.presentation.feature.auth.EnterPinScreen
import com.shiplocate.presentation.feature.auth.EnterPinViewModel
import com.shiplocate.presentation.feature.home.HomeScreen
import com.shiplocate.presentation.feature.home.HomeViewModel
import com.shiplocate.presentation.feature.loads.LoadsScreen
import com.shiplocate.presentation.feature.loads.LoadsViewModel
import com.shiplocate.presentation.feature.logs.LogsScreen
import com.shiplocate.presentation.feature.logs.LogsViewModel
import kotlinx.coroutines.launch

/**
 * Навигация с использованием строковых маршрутов
 * (Type-safe args не поддерживаются в KMP)
 *
 * Проверяет наличие токена при старте
 * 
 * @param onNavControllerReady Callback для передачи navController и currentRoute наружу
 */
@Suppress("FunctionName")
@Composable
fun TrackerNavigation(
    onNavControllerReady: (NavController, String?) -> Unit = { _, _ -> },
) {
    val navController = rememberNavController()
    val hasAuthSessionUseCase: HasAuthSessionUseCase = koinInject()
    val scope = rememberCoroutineScope()

    var isCheckingAuth by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf(Screen.ENTER_PHONE) }
    
    // Отслеживаем текущий route для передачи наружу
    var currentRoute by remember { mutableStateOf<String?>(null) }
    
    // Отслеживаем изменения в navigation state через listener для надежности
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val route = destination.route
            if (route != null) {
                currentRoute = route
                onNavControllerReady(navController, route)
            }
        }
        navController.addOnDestinationChangedListener(listener)
        
        // Инициализируем текущий route
        val initialRoute = navController.currentBackStackEntry?.destination?.route
        if (initialRoute != null) {
            currentRoute = initialRoute
            onNavControllerReady(navController, initialRoute)
        }
        
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
    
    // Также отслеживаем через state для немедленного обновления UI
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val route = navBackStackEntry?.destination?.route
    
    LaunchedEffect(route) {
        if (route != null && route != currentRoute) {
            currentRoute = route
            onNavControllerReady(navController, route)
        }
    }

    // Check auth session on start
    LaunchedEffect(Unit) {
        scope.launch {
            val hasSession = hasAuthSessionUseCase()
            startDestination = if (hasSession) Screen.LOADS else Screen.ENTER_PHONE
            currentRoute = startDestination
            isCheckingAuth = false
            onNavControllerReady(navController, startDestination)
        }
    }

    if (isCheckingAuth) {
        // Show loading or splash screen
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // Auth screens
        composable(Screen.ENTER_PHONE) {
            val enterPhoneViewModelFactory = viewModelFactory {
                initializer<EnterPhoneViewModel> {
                    koinEnterPhoneViewModel()
                }
            }
            val viewModel: EnterPhoneViewModel = viewModel(
                factory = enterPhoneViewModelFactory,
            )
            EnterPhoneScreen(
                onNavigateToPin = { phone ->
                    navController.navigate(Screen.enterPin(phone))
                },
                onNavigateToLogs = {
                    navController.navigate(Screen.LOGS)
                },
                viewModel = viewModel,
            )
        }

        composable(
            route = Screen.ENTER_PIN,
            arguments =
                listOf(
                    navArgument("phone") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val phone = backStackEntry.getStringArgument("phone") ?: ""
            val enterPinViewModelFactory = viewModelFactory {
                initializer<EnterPinViewModel> {
                    koinEnterPinViewModel()
                }
            }
            val viewModel: EnterPinViewModel = viewModel(
                factory = enterPinViewModelFactory,
            )

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
                viewModel = viewModel,
            )
        }

        // Main app screens
        composable(Screen.LOADS) {
            val loadsViewModelFactory = viewModelFactory {
                initializer<LoadsViewModel> {
                    koinLoadsViewModel()
                }
            }
            val viewModel: LoadsViewModel = viewModel(
                factory = loadsViewModelFactory,
            )
            
            // Отслеживаем текущий backStackEntry для обновления данных при возврате
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            
            // Обновляем данные из кеша когда текущий экран - LoadsScreen
            // Это сработает при первом открытии и при возврате на экран
            LaunchedEffect(currentBackStackEntry?.destination?.route) {
                if (currentBackStackEntry?.destination?.route == Screen.LOADS) {
                    viewModel.fetchLoadsFromCache()
                }
            }
            
            LoadsScreen(
                viewModel = viewModel,
                onLoadClick = { loadId ->
                    navController.navigate(Screen.home(loadId))
                },
                onNavigateToLogs = {
                    navController.navigate(Screen.LOGS)
                },
            )
        }

        composable(
            route = Screen.HOME,
            arguments =
                listOf(
                    navArgument("loadId") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val homeViewModelFactory = viewModelFactory {
                    initializer<HomeViewModel> {
                        koinHomeViewModel()
                    }
                }
            val loadId = backStackEntry.getStringArgument("loadId") ?: ""
            val viewModel: HomeViewModel = viewModel(
                    factory = homeViewModelFactory,
                )
            HomeScreen(
                loadId = loadId,
                viewModel = viewModel,
                onNavigateToLogs = {
                    navController.navigate(Screen.LOGS)
                },
            )
        }

        // Logs screen
        composable(Screen.LOGS) {
            val logsViewModelFactory =
                viewModelFactory {
                    initializer<LogsViewModel> {
                        koinLogsViewModel()
                    }
                }
            val viewModel: LogsViewModel =
                viewModel(
                    factory = logsViewModelFactory,
                )
            LogsScreen(
                viewModel = viewModel,
            )
        }
    }
}
