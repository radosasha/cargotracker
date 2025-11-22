package com.shiplocate.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shiplocate.domain.usecase.auth.HasAuthSessionUseCase
import com.shiplocate.presentation.di.koinEnterPhoneViewModel
import com.shiplocate.presentation.di.koinEnterPinViewModel
import com.shiplocate.presentation.di.koinInject
import com.shiplocate.presentation.di.koinLoadViewModel
import com.shiplocate.presentation.di.koinLoadsViewModel
import com.shiplocate.presentation.di.koinLogsViewModel
import com.shiplocate.presentation.di.koinPermissionsViewModel
import com.shiplocate.presentation.feature.auth.EnterPhoneScreen
import com.shiplocate.presentation.feature.auth.EnterPhoneViewModel
import com.shiplocate.presentation.feature.auth.EnterPinScreen
import com.shiplocate.presentation.feature.auth.EnterPinViewModel
import com.shiplocate.presentation.feature.home.LoadScreen
import com.shiplocate.presentation.feature.home.LoadViewModel
import com.shiplocate.presentation.feature.loads.LoadsScreen
import com.shiplocate.presentation.feature.loads.LoadsViewModel
import com.shiplocate.presentation.feature.logs.LogsScreen
import com.shiplocate.presentation.feature.logs.LogsViewModel
import com.shiplocate.presentation.feature.permissions.PermissionsScreen
import com.shiplocate.presentation.feature.permissions.PermissionsViewModel
import com.shiplocate.presentation.model.BottomBarState
import kotlinx.coroutines.launch

/**
 * Навигация с использованием строковых маршрутов
 * (Type-safe args не поддерживаются в KMP)
 *
 * Проверяет наличие токена при старте
 *
 * @param paddingValues PaddingValues из Scaffold для передачи в экраны
 * @param onNavControllerReady Callback для передачи navController и currentRoute наружу
 * @param onBottomBarStateChanged Callback для передачи состояния bottomBar наружу
 */
@Suppress("FunctionName")
@Composable
fun TrackerNavigation(
    paddingValues: PaddingValues,
    onNavControllerReady: (NavController, String?) -> Unit = { _, _ -> },
    onBottomBarStateChanged: (BottomBarState) -> Unit = {},
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
                paddingValues = paddingValues,
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
                paddingValues = paddingValues,
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

            // Отслеживаем текущую страницу для bottomBar
            val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()

            // Передаем состояние bottomBar в MainScreen
            LaunchedEffect(currentRoute, currentPage) {
                if (currentRoute == Screen.LOADS) {
                    onBottomBarStateChanged(
                        BottomBarState.Loads(
                            currentPage = currentPage,
                            onPageSelected = { page ->
                                viewModel.setCurrentPage(page)
                            },
                        ),
                    )
                } else {
                    onBottomBarStateChanged(BottomBarState.None)
                }
            }

            // Отслеживаем текущий backStackEntry для обновления данных при возврате
            val currentBackStackEntry by navController.currentBackStackEntryAsState()

            // Обновляем данные из кеша когда текущий экран - LoadsScreen
            // Это сработает при первом открытии и при возврате на экран
            LaunchedEffect(currentBackStackEntry?.destination?.route) {
                val currentRoute = currentBackStackEntry?.destination?.route
                if (currentRoute == Screen.LOADS) {
                    viewModel.fetchLoadsFromCache()
                    // Проверяем, был ли возврат после reject
                    val savedStateHandle = currentBackStackEntry?.savedStateHandle
                    val wasRejected = savedStateHandle?.get<Boolean>("rejectSuccess") ?: false
                    if (wasRejected) {
                        viewModel.showRejectSuccessDialog()
                        savedStateHandle?.remove<Boolean>("rejectSuccess")
                    }
                    // Проверяем, нужно ли переключиться на вкладку Active
                    val switchToActive = savedStateHandle?.get<Boolean>("switchToActive") ?: false
                    if (switchToActive) {
                        viewModel.setCurrentPage(0) // Переключаемся на вкладку Active (страница 0)
                        savedStateHandle?.remove<Boolean>("switchToActive")
                    }
                    // Проверяем, нужно ли стартовать трекинг после возврата с экрана разрешений
                    val startTracking = savedStateHandle?.get<Boolean>("startTracking") ?: false
                    if (startTracking) {
                        viewModel.startTrackingForActiveLoad()
                        savedStateHandle?.remove<Boolean>("startTracking")
                    }
                }
            }

            LoadsScreen(
                paddingValues = paddingValues,
                viewModel = viewModel,
                onLoadClick = { loadId ->
                    navController.navigate(Screen.load(loadId))
                },
                onNavigateToPermissions = {
                    navController.navigate(Screen.PERMISSIONS)
                },
            )
        }

        composable(
            route = Screen.LOAD,
            arguments =
                listOf(
                    navArgument("loadId") { type = NavType.LongType },
                ),
        ) { backStackEntry ->
            // Скрываем bottomBar при переходе на другие экраны
            LaunchedEffect(currentRoute) {
                if (currentRoute != Screen.LOADS) {
                    onBottomBarStateChanged(BottomBarState.None)
                }
            }

            val loadViewModelFactory = viewModelFactory {
                initializer<LoadViewModel> {
                    koinLoadViewModel()
                }
            }
            val loadId = backStackEntry.getLongArgument("loadId") ?: 0L
            val viewModel: LoadViewModel = viewModel(
                factory = loadViewModelFactory,
            )

            // Обновляем данные из кеша когда текущий экран - LoadsScreen
            // Это сработает при первом открытии и при возврате на экран
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            LaunchedEffect(currentBackStackEntry?.destination?.route) {
                val currentRoute = currentBackStackEntry?.destination?.route
                if (currentRoute == Screen.LOAD) {
                    if (currentRoute == Screen.LOAD) {
                        val savedStateHandle = currentBackStackEntry?.savedStateHandle
                        val startTracking = savedStateHandle?.get<Boolean>("startTracking") ?: false
                        if (startTracking) {
                            viewModel.startTracking()
                            savedStateHandle?.remove<Boolean>("startTracking")
                        }
                    }
                }
            }

            LoadScreen(
                paddingValues = paddingValues,
                loadId = loadId,
                viewModel = viewModel,
                onNavigateToLogs = {
                    navController.navigate(Screen.LOGS)
                },
                onNavigateBack = { wasRejected, switchToActive ->
                    val loadsEntry = navController.getBackStackEntry(Screen.LOADS)
                    if (wasRejected) {
                        // Устанавливаем флаг в savedStateHandle для LoadsScreen
                        loadsEntry.savedStateHandle["rejectSuccess"] = true
                    }
                    if (switchToActive) {
                        // Устанавливаем флаг для переключения на вкладку Active
                        loadsEntry.savedStateHandle["switchToActive"] = true
                    }
                    navController.popBackStack()
                },
                onNavigateToPermissions = {
                    navController.navigate(Screen.PERMISSIONS)
                },
            )
        }

        // Permissions screen
        composable(Screen.PERMISSIONS) {
            // Скрываем bottomBar при переходе на другие экраны
            LaunchedEffect(currentRoute) {
                if (currentRoute != Screen.LOADS) {
                    onBottomBarStateChanged(BottomBarState.None)
                }
            }

            val permissionsViewModelFactory = viewModelFactory {
                initializer<PermissionsViewModel> {
                    koinPermissionsViewModel()
                }
            }
            val viewModel: PermissionsViewModel = viewModel(
                factory = permissionsViewModelFactory,
            )


            PermissionsScreen(
                paddingValues = paddingValues,
                viewModel = viewModel,
                onContinue = {
                    // Получаем savedStateHandle из LoadsScreen для передачи флага старта трекинга
                    val previousBackStackEntry = navController.previousBackStackEntry
                    // Устанавливаем флаг для автоматического старта трекинга
                    // Это сработает только если пользователь пришел с LoadsScreen
                    previousBackStackEntry?.savedStateHandle["startTracking"] = true
                    navController.popBackStack()
                },
            )
        }

        // Logs screen
        composable(Screen.LOGS) {
            // Скрываем bottomBar при переходе на другие экраны
            LaunchedEffect(currentRoute) {
                if (currentRoute != Screen.LOADS) {
                    onBottomBarStateChanged(BottomBarState.None)
                }
            }

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
                paddingValues = paddingValues,
                viewModel = viewModel,
            )
        }
    }
}
