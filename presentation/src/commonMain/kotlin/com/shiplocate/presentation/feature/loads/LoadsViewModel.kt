package com.shiplocate.presentation.feature.loads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.model.load.LoadStatus
import com.shiplocate.domain.model.notification.NotificationType
import com.shiplocate.domain.usecase.GetActiveLoadUseCase
import com.shiplocate.domain.usecase.GetPermissionStatusUseCase
import com.shiplocate.domain.usecase.ObservePermissionsUseCase
import com.shiplocate.domain.usecase.ObserveReceivedPushesUseCase
import com.shiplocate.domain.usecase.RequestNotificationPermissionUseCase
import com.shiplocate.domain.usecase.SendCachedTokenOnAuthUseCase
import com.shiplocate.domain.usecase.StartTrackingUseCase
import com.shiplocate.domain.usecase.StopTrackingIfLoadUnlinkedUseCase
import com.shiplocate.domain.usecase.StopTrackingUseCase
import com.shiplocate.domain.usecase.auth.LogoutUseCase
import com.shiplocate.domain.usecase.load.DisconnectFromLoadUseCase
import com.shiplocate.domain.usecase.load.GetCachedLoadsUseCase
import com.shiplocate.domain.usecase.load.GetCachedRouteUseCase
import com.shiplocate.domain.usecase.load.GetLoadsUseCase
import com.shiplocate.domain.usecase.load.ObserveCachedRouteUseCase
import com.shiplocate.domain.usecase.load.RejectLoadUseCase
import com.shiplocate.domain.usecase.load.UpdateStopCompletionUseCase
import com.shiplocate.presentation.mapper.toActiveLoadUiModel
import com.shiplocate.presentation.mapper.toUiModel
import com.shiplocate.presentation.model.ActiveLoadUiModel
import com.shiplocate.presentation.model.LoadUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for Loads screen
 * Manages loading and displaying loads list
 */
class LoadsViewModel(
    private val getLoadsUseCase: GetLoadsUseCase,
    private val getCachedLoadsUseCase: GetCachedLoadsUseCase,
    private val getActiveLoadUseCase: GetActiveLoadUseCase,
    private val startTrackingUseCase: StartTrackingUseCase,
    private val stopTrackingUseCase: StopTrackingUseCase,
    private val permissionStatusUseCase: GetPermissionStatusUseCase,
    private val observePermissionsUseCase: ObservePermissionsUseCase,
    private val disconnectFromLoadUseCase: DisconnectFromLoadUseCase,
    private val rejectLoadUseCase: RejectLoadUseCase,
    private val updateStopCompletionUseCase: UpdateStopCompletionUseCase,
    private val requestNotificationPermissionUseCase: RequestNotificationPermissionUseCase,
    private val sendCachedTokenOnAuthUseCase: SendCachedTokenOnAuthUseCase,
    private val observeReceivedPushesUseCase: ObserveReceivedPushesUseCase,
    private val stopTrackingIfLoadUnlinkedUseCase: StopTrackingIfLoadUnlinkedUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCachedRouteUseCase: GetCachedRouteUseCase,
    private val observeCachedRouteUseCase: ObserveCachedRouteUseCase,
    private val logger: Logger,
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoadsUiState>(LoadsUiState.Loading)
    val uiState: StateFlow<LoadsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _showRejectSuccessDialog = MutableStateFlow(false)
    val showRejectSuccessDialog: StateFlow<Boolean> = _showRejectSuccessDialog.asStateFlow()

    private val _showLoadDeliveredDialog = MutableStateFlow(false)
    val showLoadDeliveredDialog: StateFlow<Boolean> = _showLoadDeliveredDialog.asStateFlow()

    private val _showRejectLoadDialog = MutableStateFlow(false)
    val showRejectLoadDialog: StateFlow<Boolean> = _showRejectLoadDialog.asStateFlow()

    private val _isLoadingAction = MutableStateFlow(false)
    val isLoadingAction: StateFlow<Boolean> = _isLoadingAction.asStateFlow()

    private val _showTrackingStartedSuccessDialog = MutableStateFlow(false)
    val showTrackingStartedSuccessDialog: StateFlow<Boolean> = _showTrackingStartedSuccessDialog.asStateFlow()

    private val _showLogoutDialog = MutableStateFlow(false)
    val showLogoutDialog: StateFlow<Boolean> = _showLogoutDialog.asStateFlow()

    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

    private val _shouldNavigateToLogin = MutableStateFlow(false)
    val shouldNavigateToLogin: StateFlow<Boolean> = _shouldNavigateToLogin.asStateFlow()

    private val _logoutError = MutableStateFlow<String?>(null)
    val logoutError: StateFlow<String?> = _logoutError.asStateFlow()

    init {
        observePermissions()
        observePushes()
        observeActiveLoadRoute()
    }

    private fun observePermissions() {
        observePermissionsUseCase()
            .onEach { status ->
                logger.debug(LogCategory.PERMISSIONS, "LoadsViewModel: Received permission status update from Flow")
                updatePermissionsWarning(status)
            }
            .launchIn(viewModelScope)
    }

    private fun observePushes() {
        // Подписываемся на push-уведомления когда приложение запущено
        observeReceivedPushesUseCase()
            .onEach { type ->
                logger.info(
                    LogCategory.GENERAL,
                    "LoadsViewModel: Received push notification (type=$type), refreshing loads",
                )
                if (type in listOf(
                        NotificationType.NEW_LOAD,
                        NotificationType.LOAD_UPDATED,
                        NotificationType.LOAD_ASSIGNED,
                        NotificationType.LOAD_UNAVAILABLE
                    )
                ) {
                    refresh()
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Observes route changes for active load and updates UI automatically
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeActiveLoadRoute() {
        _uiState.map { state ->
            when (state) {
                is LoadsUiState.Success -> state.activeLoad?.id
                else -> null
            }
        }
            .distinctUntilChanged()
            .flatMapLatest { activeLoadId ->
                if (activeLoadId != null) {
                    // Filter by loadId in flatMapLatest
                    observeCachedRouteUseCase().map { route ->
                        route
                    }
                } else {
                    flowOf(null)
                }
            }
            .onEach { route ->
                logger.debug(
                    LogCategory.UI,
                    "LoadsViewModel: Route updated for active load, duration=${route?.duration}"
                )
                val currentState = _uiState.value
                if (currentState is LoadsUiState.Success) {
                    val activeLoad = currentState.activeLoad
                    if (activeLoad != null) {
                        val routeDuration = formatRouteDuration(route?.duration)
                        val updatedActiveLoad = activeLoad.copy(routeDuration = routeDuration)
                        _uiState.value = currentState.copy(activeLoad = updatedActiveLoad)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun updatePermissionsWarning(permissionStatus: com.shiplocate.domain.model.PermissionStatus) {
        val currentState = _uiState.value
        if (currentState is LoadsUiState.Success) {
            val activeLoad = currentState.activeLoad
            // Показываем красное окно, если:
            // 1. Есть активный Load
            // 2. Не все разрешения для трекинга получены
            val showWarning = activeLoad != null && !permissionStatus.hasAllPermissionsForTracking

            _uiState.value = currentState.copy(showPermissionsWarning = showWarning)
        }
    }

    /**
     * Стартует трекинг для активного Load
     * Вызывается когда пользователь вернулся с экрана разрешений после выдачи всех разрешений
     */
    fun startTrackingForActiveLoad() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is LoadsUiState.Success) {
                    val activeLoad = currentState.activeLoad
                    if (activeLoad != null) {
                        logger.info(LogCategory.UI, "LoadsViewModel: Starting tracking for active load ${activeLoad.id}")

                        val result = withContext(Dispatchers.Default) {
                            startTrackingUseCase(activeLoad.id)
                        }
                        if (result.isSuccess) {
                            logger.info(LogCategory.UI, "LoadsViewModel: Successfully started tracking for load ${activeLoad.id}")
                            // Показываем диалог успешного запуска трекинга
                            _showTrackingStartedSuccessDialog.value = true
                            // Скрываем предупреждение о разрешениях, так как трекинг запущен
                            val updatedState = currentState.copy(showPermissionsWarning = false)
                            _uiState.value = updatedState
                        } else {
                            logger.error(
                                LogCategory.UI,
                                "LoadsViewModel: Failed to start tracking: ${result.exceptionOrNull()?.message}",
                            )
                        }

                    } else {
                        logger.warn(LogCategory.UI, "LoadsViewModel: No active load to start tracking")
                    }
                }
            } catch (e: Exception) {
                logger.error(LogCategory.UI, "LoadsViewModel: Exception during start tracking: ${e.message}")
            }
        }
    }

    /**
     * Set current page for pager
     */
    fun setCurrentPage(page: Int) {
        _currentPage.value = page
    }

    /**
     * Show reject success dialog
     */
    fun showRejectSuccessDialog() {
        _showRejectSuccessDialog.value = true
    }

    /**
     * Dismiss reject success dialog
     */
    fun dismissRejectSuccessDialog() {
        _showRejectSuccessDialog.value = false
    }

    init {
        logger.info(LogCategory.UI, "LoadsViewModel: Initialized")
        fetchLoadsFromCache()
        checkAndRestoreTracking()
        requestNotificationPermission()
        sendCachedTokenOnStartup()
        loadLoads()
    }

    /**
     * Проверяет, был ли запущен трекинг при предыдущем запуске
     * Если в DataStore сохранено true, автоматически запускает трекинг
     */
    fun checkAndRestoreTracking() {
        viewModelScope.launch {
            try {
                logger.info(LogCategory.LOCATION, "LoadsViewModel: Checking if tracking was active before...")

                // првоеряем пермишены требуемые для трекинга
                val permissions = permissionStatusUseCase()
                if (!permissions.hasAllPermissionsForTracking) {
                    logger.info(
                        LogCategory.LOCATION,
                        "LoadsViewModel: Can't automatically start tracking. Need some permissions for tracking"
                    )
                    return@launch
                }


                // Проверяем состояние из DataStore
                val isTrackingActive = withContext(Dispatchers.Default) {
                    getActiveLoadUseCase() != null
                }

                if (isTrackingActive) {
                    logger.info(LogCategory.LOCATION, "LoadsViewModel: Tracking was active before, restoring...")

                    // Получаем первый load из кеша для восстановления трекинга
                    val loads = getCachedLoadsUseCase()
                    val firstLoad = loads.firstOrNull()

                    if (firstLoad != null) {
                        // Автоматически запускаем трекинг с loadId
                        val result =
                            withContext(Dispatchers.Default) {
                                startTrackingUseCase(firstLoad.id)
                            }
                        if (result.isSuccess) {
                            logger.info(LogCategory.LOCATION, "LoadsViewModel: Tracking restored successfully with loadId=${firstLoad.id}")
                        } else {
                            logger.error(
                                LogCategory.LOCATION,
                                "LoadsViewModel: Failed to restore tracking: ${result.exceptionOrNull()?.message}",
                            )
                        }
                    } else {
                        logger.warn(LogCategory.LOCATION, "LoadsViewModel: No loads found in cache, cannot restore tracking")
                    }
                } else {
                    logger.info(LogCategory.LOCATION, "LoadsViewModel: Tracking was not active, no restoration needed")
                }
            } catch (e: Exception) {
                logger.error(LogCategory.LOCATION, "LoadsViewModel: Error checking tracking state: ${e.message}")
            }
        }
    }

    /**
     * Load loads from server or cache
     * @param isRefresh true if triggered by pull-to-refresh
     */
    fun loadLoads(isRefresh: Boolean = false) {
        logger.info(LogCategory.UI, "LoadsViewModel: Loading loads (refresh: $isRefresh)")

        if (isRefresh) {
            _isRefreshing.value = true
        } else {
            _uiState.value = LoadsUiState.Loading
        }

        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                getLoadsUseCase()
            }

            result.fold(
                onSuccess = { loads ->
                    logger.info(LogCategory.UI, "LoadsViewModel: Successfully loaded ${loads.size} loads")
                    showLoadsOnUI(loads)
                    _isRefreshing.value = false
                },
                onFailure = { error ->
                    logger.error(LogCategory.UI, "LoadsViewModel: Failed to load loads: ${error.message}")
                    _isRefreshing.value = false
                    _uiState.value =
                        LoadsUiState.Error(
                            error.message ?: "Failed to load loads",
                        )
                },
            )
        }
    }

    private suspend fun showLoadsOnUI(loads: List<Load>) {
        // Сортируем список по дате создания (createdAt) - новые сверху
        val sortedLoads = loads.sortedBy { it.createdAt }

        // Разделяем на Active (LOAD_STATUS_CONNECTED) и Upcoming (LOAD_STATUS_NOT_CONNECTED)
        val activeLoad = sortedLoads.firstOrNull { it.loadStatus == LoadStatus.LOAD_STATUS_CONNECTED }
        val upcomingLoads = sortedLoads.filter { it.loadStatus == LoadStatus.LOAD_STATUS_NOT_CONNECTED }

        if (activeLoad != null) {
            stopTrackingIfLoadUnlinkedUseCase(activeLoad)
        }

        // Получаем текущий статус разрешений для расчета showPermissionsWarning
        val permissionStatus = withContext(Dispatchers.Default) {
            permissionStatusUseCase()
        }
        val showWarning = activeLoad != null && !permissionStatus.hasAllPermissionsForTracking

        // Route will be updated automatically via observeActiveLoadRoute()

        val activeLoadUi = activeLoad?.let {
            val cachedRoute = getCachedRouteUseCase(it.id)
            val routeDuration = formatRouteDuration(cachedRoute?.duration)
            it.toActiveLoadUiModel(routeDuration = routeDuration)
        }

        _uiState.value = LoadsUiState.Success(
            activeLoad = activeLoadUi,
            upcomingLoads = upcomingLoads.map { it.toUiModel() },
            showPermissionsWarning = showWarning,
        )
    }

    /**
     * Retry loading loads
     */
    fun retry() {
        logger.info(LogCategory.UI, "LoadsViewModel: Retrying")
        loadLoads()
    }

    /**
     * Refresh loads (pull-to-refresh)
     */
    fun refresh() {
        logger.info(LogCategory.UI, "LoadsViewModel: Refreshing via pull-to-refresh")
        loadLoads(isRefresh = true)
    }

    /**
     * Load loads from cache only (called when returning from HomeScreen)
     */
    fun fetchLoadsFromCache() {
        logger.info(LogCategory.UI, "LoadsViewModel: Loading from cache")

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val cachedLoads = withContext(Dispatchers.Default) {
                    getCachedLoadsUseCase()
                }
                logger.info(LogCategory.UI, "LoadsViewModel: Successfully loaded ${cachedLoads.size} loads from cache")

                showLoadsOnUI(cachedLoads)
            } catch (e: Exception) {
                logger.error(LogCategory.UI, "LoadsViewModel: Failed to load from cache: ${e.message}")
                _uiState.value =
                    LoadsUiState.Error(
                        e.message ?: "Failed to load cached data",
                    )
            }
        }
    }

    /**
     * Запрашивает разрешения на уведомления
     * Вызывается после успешной авторизации пользователя
     */
    private fun requestNotificationPermission() {
        viewModelScope.launch {
            try {
                logger.info(LogCategory.PERMISSIONS, "LoadsViewModel: Requesting notification permission...")
                val result = requestNotificationPermissionUseCase()

                if (result.isSuccess) {
                    val granted = result.getOrNull() ?: false
                    if (granted) {
                        logger.info(LogCategory.PERMISSIONS, "LoadsViewModel: Notification permission granted")
                    } else {
                        logger.warn(LogCategory.PERMISSIONS, "LoadsViewModel: Notification permission denied")
                    }
                } else {
                    logger.error(
                        LogCategory.PERMISSIONS,
                        "LoadsViewModel: Failed to request notification permission: ${result.exceptionOrNull()?.message}",
                    )
                }
            } catch (e: Exception) {
                logger.error(LogCategory.PERMISSIONS, "LoadsViewModel: Exception while requesting notification permission: ${e.message}")
            }
        }
    }

    /**
     * Отправляет закешированный Firebase токен при запуске приложения
     * Вызывается при инициализации LoadsViewModel
     */
    private fun sendCachedTokenOnStartup() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                logger.info(LogCategory.NETWORK, "LoadsViewModel: Attempting to send cached Firebase token on startup...")
                sendCachedTokenOnAuthUseCase()
            } catch (e: Exception) {
                logger.error(LogCategory.NETWORK, "LoadsViewModel: Failed to send cached token on startup: ${e.message}")
            }
        }
    }

    /**
     * Show load delivered dialog
     */
    fun showLoadDeliveredDialog() {
        _showLoadDeliveredDialog.value = true
    }

    /**
     * Dismiss load delivered dialog
     */
    fun dismissLoadDeliveredDialog() {
        _showLoadDeliveredDialog.value = false
    }

    /**
     * Show reject load dialog
     */
    fun showRejectLoadDialog() {
        _showRejectLoadDialog.value = true
    }

    /**
     * Dismiss reject load dialog
     */
    fun dismissRejectLoadDialog() {
        _showRejectLoadDialog.value = false
    }

    /**
     * Dismiss tracking started success dialog
     */
    fun dismissTrackingStartedSuccessDialog() {
        _showTrackingStartedSuccessDialog.value = false
    }

    /**
     * Show logout confirmation dialog
     */
    fun showLogoutDialog() {
        _showLogoutDialog.value = true
    }

    /**
     * Dismiss logout dialog
     */
    fun dismissLogoutDialog() {
        _showLogoutDialog.value = false
    }

    /**
     * Confirm logout and perform logout process
     */
    fun confirmLogout() {
        viewModelScope.launch {
            _showLogoutDialog.value = false
            _isLoggingOut.value = true
            _logoutError.value = null

            try {
                logger.info(LogCategory.AUTH, "LoadsViewModel: Starting logout process")
                val result = withContext(Dispatchers.Default) {
                    logoutUseCase()
                }

                if (result.isSuccess) {
                    logger.info(LogCategory.AUTH, "LoadsViewModel: Logout successful, navigating to login")
                    _shouldNavigateToLogin.value = true
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Logout failed"
                    logger.error(
                        LogCategory.AUTH,
                        "LoadsViewModel: Logout failed: $errorMessage",
                    )
                    _logoutError.value = errorMessage
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unexpected error occurred during logout"
                logger.error(LogCategory.AUTH, "LoadsViewModel: Exception during logout: $errorMessage", e)
                _logoutError.value = errorMessage
            } finally {
                _isLoggingOut.value = false
            }
        }
    }

    /**
     * Reset navigation flag after navigation
     */
    fun resetNavigateToLoginFlag() {
        _shouldNavigateToLogin.value = false
    }

    /**
     * Dismiss logout error dialog
     */
    fun dismissLogoutError() {
        _logoutError.value = null
    }

    /**
     * Confirm load delivered
     */
    fun confirmLoadDelivered(loadId: Long) {
        viewModelScope.launch {
            _showLoadDeliveredDialog.value = false
            _isLoadingAction.value = true

            try {
                logger.info(LogCategory.UI, "LoadsViewModel: Marking load as delivered (id: $loadId)")

                // Step 1: Disconnect from load (marks as delivered)
                val disconnectResult =
                    withContext(Dispatchers.Default) {
                        disconnectFromLoadUseCase(loadId)
                    }
                if (disconnectResult.isFailure) {
                    logger.error(
                        LogCategory.UI,
                        "LoadsViewModel: Failed to mark load as delivered: ${disconnectResult.exceptionOrNull()?.message}",
                    )
                    _isLoadingAction.value = false
                    // Обновляем список loads после ошибки
                    fetchLoadsFromCache()
                    return@launch
                }

                logger.info(LogCategory.UI, "LoadsViewModel: Successfully marked load as delivered")

                // Step 2: Stop tracking
                val result = stopTrackingUseCase()
                if (result.isFailure) {
                    logger.error(LogCategory.UI, "LoadsViewModel: Failed to stop tracking: ${result.exceptionOrNull()?.message}")
                } else {
                    logger.info(LogCategory.UI, "LoadsViewModel: Successfully stopped tracking after marking load as delivered")
                }

                // Обновляем список loads
                fetchLoadsFromCache()
            } catch (e: Exception) {
                logger.error(LogCategory.UI, "LoadsViewModel: Exception during mark load as delivered: ${e.message}")
            } finally {
                _isLoadingAction.value = false
            }
        }
    }

    /**
     * Confirm reject load
     */
    fun confirmRejectLoad(loadId: Long) {
        viewModelScope.launch {
            _showRejectLoadDialog.value = false
            _isLoadingAction.value = true

            try {
                logger.info(LogCategory.UI, "LoadsViewModel: Rejecting load (id: $loadId)")

                // Reject load
                val result =
                    withContext(Dispatchers.Default) {
                        rejectLoadUseCase(loadId)
                    }

                if (result.isSuccess) {
                    logger.info(LogCategory.UI, "LoadsViewModel: Successfully rejected load")
                    // Обновляем список loads
                    fetchLoadsFromCache()
                } else {
                    logger.error(
                        LogCategory.UI,
                        "LoadsViewModel: Failed to reject load: ${result.exceptionOrNull()?.message}",
                    )
                    // Обновляем список loads даже при ошибке
                    fetchLoadsFromCache()
                }
            } catch (e: Exception) {
                logger.error(LogCategory.UI, "LoadsViewModel: Exception during reject load: ${e.message}")
            } finally {
                _isLoadingAction.value = false
            }
        }
    }

    fun updateStopCompletion(stopId: Long, completion: Int) {
        viewModelScope.launch {
            _isLoadingAction.value = true

            try {
                logger.info(LogCategory.UI, "LoadsViewModel: Updating stop completion for stop $stopId to $completion")

                val result = withContext(Dispatchers.IO) {
                    updateStopCompletionUseCase(stopId, completion)
                }

                if (result.isSuccess) {
                    logger.info(LogCategory.UI, "LoadsViewModel: Successfully updated stop completion")
                    // Refresh loads to get updated stops
                    refresh()
                } else {
                    logger.error(
                        LogCategory.UI,
                        "LoadsViewModel: Failed to update stop completion: ${result.exceptionOrNull()?.message}",
                    )
                }
            } catch (e: Exception) {
                logger.error(LogCategory.UI, "LoadsViewModel: Exception during update stop completion: ${e.message}")
            } finally {
                _isLoadingAction.value = false
            }
        }
    }

    private fun formatRouteDuration(durationSeconds: Int?): String? {
        if (durationSeconds == null) return null
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val hoursPart = hours.toString().padStart(2, '0')
        val minutesPart = minutes.toString().padStart(2, '0')
        return "${hoursPart}h:${minutesPart}m"
    }
}

/**
 * UI State for Loads screen
 */
sealed class LoadsUiState {
    data object Loading : LoadsUiState()

    data class Success(
        val activeLoad: ActiveLoadUiModel? = null, // Один Load для первой вкладки (Active)
        val upcomingLoads: List<LoadUiModel> = emptyList(), // Список Load для второй вкладки (Upcoming)
        val showPermissionsWarning: Boolean = false, // Показывать красное окно с предупреждением о разрешениях
    ) : LoadsUiState()

    data class Error(val message: String) : LoadsUiState()
}
