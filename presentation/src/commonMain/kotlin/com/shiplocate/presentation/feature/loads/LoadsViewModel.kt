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
import com.shiplocate.domain.usecase.load.GetCachedLoadsUseCase
import com.shiplocate.domain.usecase.load.GetLoadsUseCase
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
    private val requestNotificationPermissionUseCase: RequestNotificationPermissionUseCase,
    private val sendCachedTokenOnAuthUseCase: SendCachedTokenOnAuthUseCase,
    private val logger: Logger,
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoadsUiState>(LoadsUiState.Loading)
    val uiState: StateFlow<LoadsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

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

                    // Автоматически запускаем трекинг
                    val result =
                        withContext(Dispatchers.Default) {
                            startTrackingUseCase()
                        }
                    if (result.isSuccess) {
                        logger.info(LogCategory.LOCATION, "LoadsViewModel: Tracking restored successfully")
                    } else {
                        logger.error(LogCategory.LOCATION, "LoadsViewModel: Failed to restore tracking: ${result.exceptionOrNull()?.message}")
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
                    
                    _isRefreshing.value = false
                    _uiState.value = LoadsUiState.Success(sortedLoads)
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
                
                _uiState.value = LoadsUiState.Success(sortedCachedLoads)
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
                    logger.error(LogCategory.PERMISSIONS, "LoadsViewModel: Failed to request notification permission: ${result.exceptionOrNull()?.message}")
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
}

/**
 * UI State for Loads screen
 */
sealed class LoadsUiState {
    data object Loading : LoadsUiState()

    data class Success(val loads: List<Load>) : LoadsUiState()

    data class Error(val message: String) : LoadsUiState()
}
