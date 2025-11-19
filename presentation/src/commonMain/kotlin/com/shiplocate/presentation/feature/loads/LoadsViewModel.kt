package com.shiplocate.presentation.feature.loads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.usecase.GetTrackingStatusUseCase
import com.shiplocate.domain.usecase.RequestNotificationPermissionUseCase
import com.shiplocate.domain.usecase.SendCachedTokenOnAuthUseCase
import com.shiplocate.domain.usecase.StartTrackingUseCase
import com.shiplocate.domain.usecase.StopTrackingUseCase
import com.shiplocate.domain.usecase.load.DisconnectFromLoadUseCase
import com.shiplocate.domain.usecase.load.GetCachedLoadsUseCase
import com.shiplocate.domain.usecase.load.GetLoadsUseCase
import com.shiplocate.domain.usecase.load.RejectLoadUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for Loads screen
 * Manages loading and displaying loads list
 */
class LoadsViewModel(
    private val getLoadsUseCase: GetLoadsUseCase,
    private val getCachedLoadsUseCase: GetCachedLoadsUseCase,
    private val getTrackingStatusUseCase: GetTrackingStatusUseCase,
    private val startTrackingUseCase: StartTrackingUseCase,
    private val stopTrackingUseCase: StopTrackingUseCase,
    private val disconnectFromLoadUseCase: DisconnectFromLoadUseCase,
    private val rejectLoadUseCase: RejectLoadUseCase,
    private val requestNotificationPermissionUseCase: RequestNotificationPermissionUseCase,
    private val sendCachedTokenOnAuthUseCase: SendCachedTokenOnAuthUseCase,
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

                // Проверяем состояние из DataStore
                val currentStatus =
                    withContext(Dispatchers.Default) {
                        getTrackingStatusUseCase()
                    }
                val isTrackingActive = currentStatus == com.shiplocate.domain.model.TrackingStatus.ACTIVE

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
            val result =
                withContext(Dispatchers.Default) {
                    getLoadsUseCase()
                }

            result.fold(
                onSuccess = { loads ->
                    logger.info(LogCategory.UI, "LoadsViewModel: Successfully loaded ${loads.size} loads")

                    // Сортируем список по дате создания (createdAt) - новые сверху
                    val sortedLoads = loads.sortedByDescending { it.createdAt }

                    // Разделяем на Active (loadStatus == 1) и Upcoming (loadStatus == 0)
                    val activeLoad = sortedLoads.firstOrNull { it.loadStatus == 1 }
                    val upcomingLoads = sortedLoads.filter { it.loadStatus == 0 }

                    _isRefreshing.value = false
                    _uiState.value = LoadsUiState.Success(
                        activeLoad = activeLoad,
                        upcomingLoads = upcomingLoads,
                    )
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

        viewModelScope.launch {
            try {
                val cachedLoads =
                    withContext(Dispatchers.Default) {
                        getCachedLoadsUseCase()
                    }
                logger.info(LogCategory.UI, "LoadsViewModel: Successfully loaded ${cachedLoads.size} loads from cache")

                // Сортируем кешированный список по дате создания (createdAt) - новые сверху
                val sortedCachedLoads = cachedLoads.sortedByDescending { it.createdAt }
                logger.debug(LogCategory.UI, "LoadsViewModel: Sorted ${sortedCachedLoads.size} cached loads by createdAt (newest first)")

                // Разделяем на Active (loadStatus == 1) и Upcoming (loadStatus == 0)
                val activeLoad = sortedCachedLoads.firstOrNull { it.loadStatus == 1 }
                val upcomingLoads = sortedCachedLoads.filter { it.loadStatus == 0 }

                _uiState.value = LoadsUiState.Success(
                    activeLoad = activeLoad,
                    upcomingLoads = upcomingLoads,
                )
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
}

/**
 * UI State for Loads screen
 */
sealed class LoadsUiState {
    data object Loading : LoadsUiState()

    data class Success(
        val activeLoad: Load? = null, // Один Load для первой вкладки (Active)
        val upcomingLoads: List<Load> = emptyList(), // Список Load для второй вкладки (Upcoming)
    ) : LoadsUiState()

    data class Error(val message: String) : LoadsUiState()
}
