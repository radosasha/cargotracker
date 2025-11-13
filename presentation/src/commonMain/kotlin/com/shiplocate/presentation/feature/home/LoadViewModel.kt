package com.shiplocate.presentation.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.repository.LoadRepository
import com.shiplocate.domain.usecase.GetPermissionStatusUseCase
import com.shiplocate.domain.usecase.GetTrackingStatusUseCase
import com.shiplocate.domain.usecase.ObservePermissionsUseCase
import com.shiplocate.domain.usecase.StartTrackingUseCase
import com.shiplocate.domain.usecase.StopTrackingUseCase
import com.shiplocate.domain.usecase.load.ConnectToLoadUseCase
import com.shiplocate.domain.usecase.load.DisconnectFromLoadUseCase
import com.shiplocate.domain.usecase.load.RejectLoadUseCase
import com.shiplocate.presentation.mapper.toUiModel
import com.shiplocate.presentation.model.LoadUiState
import com.shiplocate.presentation.model.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel для главного экрана
 */
class LoadViewModel(
    private val getPermissionStatusUseCase: GetPermissionStatusUseCase,
    private val observePermissionsUseCase: ObservePermissionsUseCase,
    private val getTrackingStatusUseCase: GetTrackingStatusUseCase,
    private val startTrackingUseCase: StartTrackingUseCase,
    private val stopTrackingUseCase: StopTrackingUseCase,
    private val connectToLoadUseCase: ConnectToLoadUseCase,
    private val disconnectFromLoadUseCase: DisconnectFromLoadUseCase,
    private val rejectLoadUseCase: RejectLoadUseCase,
    private val loadRepository: LoadRepository,
    private val logger: Logger,
) : ViewModel() {
    private var loadId: Long = 0L
    private val _uiState = MutableStateFlow(LoadUiState())
    val uiState: StateFlow<LoadUiState> = _uiState.asStateFlow()

    init {
        observePermissionsAndTrackingStatus()
        // Подписываемся на изменения разрешений из Flow
        observePermissionsUseCase()
            .onEach { status ->
                logger.debug(LogCategory.PERMISSIONS, "LoadViewModel: Received permission status update from Flow")
                _uiState.value = _uiState.value.copy(permissionStatus = status)
            }
            .launchIn(viewModelScope)
    }

    fun initialize(loadId: Long) {
        logger.info(LogCategory.UI, "HomeViewModel: Initialized with loadId = $loadId")

        this.loadId = loadId
        
        // Find load by id to get full load with stops for UI display
        viewModelScope.launch {
            val loads = loadRepository.getCachedLoads()
            val load = loads.find { it.id == loadId }
            _uiState.value = _uiState.value.copy(load = load?.toUiModel())
        }
    }

    private fun observePermissionsAndTrackingStatus() {
        viewModelScope.launch {
            try {
                val permissionStatus = getPermissionStatusUseCase()
                val trackingStatus = getTrackingStatusUseCase()

                _uiState.value =
                    _uiState.value.copy(
                        permissionStatus = permissionStatus,
                        trackingStatus = trackingStatus,
                        isLoading = false,
                    )
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        message = "Ошибка при загрузке статуса: ${e.message}",
                        messageType = MessageType.ERROR,
                    )
            }
        }
    }

    fun startTracking() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Step 1: Find load by id
                val loads = loadRepository.getCachedLoads()
                val load = loads.find { it.id == loadId }

                if (load == null) {
                    logger.error(LogCategory.UI, "HomeViewModel: Load not found with id: $loadId")
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            message = "Load not found: $loadId",
                            messageType = MessageType.ERROR,
                        )
                    return@launch
                }

                logger.info(LogCategory.UI, "HomeViewModel: Connecting to load ${load.loadName} (id: $loadId) before starting tracking")

                val connectResult =
                    withContext(Dispatchers.Default) {
                        connectToLoadUseCase(loadId)
                    }
                if (connectResult.isFailure) {
                    logger.error(LogCategory.UI, "HomeViewModel: Failed to connect to load: ${connectResult.exceptionOrNull()?.message}")
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            message = "Failed to connect to load: ${connectResult.exceptionOrNull()?.message}",
                            messageType = MessageType.ERROR,
                        )
                    return@launch
                }

                logger.info(LogCategory.UI, "HomeViewModel: Successfully connected to load ${load.loadName} (id: $loadId)")

                // Step 2: Start tracking
                val result = startTrackingUseCase(loadId)
                if (result.isSuccess) {
                    // Обновляем статус трекинга
                    val trackingStatus = getTrackingStatusUseCase()
                    _uiState.value =
                        _uiState.value.copy(
                            trackingStatus = trackingStatus,
                            message = "GPS трекинг запущен",
                            messageType = MessageType.SUCCESS,
                            shouldNavigateBackAfterStart = true, // Устанавливаем флаг для навигации назад
                        )
                } else {
                    _uiState.value =
                        _uiState.value.copy(
                            message = "Не удалось запустить трекинг: ${result.exceptionOrNull()?.message}",
                            messageType = MessageType.ERROR,
                        )
                }
            } catch (e: Exception) {
                logger.error(LogCategory.UI, "HomeViewModel: Exception during start tracking: ${e.message}")
                _uiState.value =
                    _uiState.value.copy(
                        message = "Ошибка при запуске трекинга: ${e.message}",
                        messageType = MessageType.ERROR,
                    )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun clearMessage() {
        _uiState.value =
            _uiState.value.copy(
                message = null,
                messageType = null,
            )
    }

    fun showLoadDeliveredDialog() {
        _uiState.value = _uiState.value.copy(showLoadDeliveredDialog = true)
    }

    fun dismissLoadDeliveredDialog() {
        _uiState.value = _uiState.value.copy(showLoadDeliveredDialog = false)
    }

    fun resetNavigateBackFlag() {
        _uiState.value = _uiState.value.copy(shouldNavigateBack = false)
    }

    fun showRejectLoadDialog() {
        _uiState.value = _uiState.value.copy(showRejectLoadDialog = true)
    }

    fun dismissRejectLoadDialog() {
        _uiState.value = _uiState.value.copy(showRejectLoadDialog = false)
    }

    fun resetNavigateBackAfterRejectFlag() {
        _uiState.value = _uiState.value.copy(shouldNavigateBackAfterReject = false)
    }

    fun resetNavigateBackAfterStartFlag() {
        _uiState.value = _uiState.value.copy(shouldNavigateBackAfterStart = false)
    }

    fun confirmRejectLoad() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showRejectLoadDialog = false,
                isLoading = true,
            )

            try {
                // Step 1: Find load by id
                val loads = loadRepository.getCachedLoads()
                val load = loads.find { it.id == loadId }

                if (load == null) {
                    logger.error(LogCategory.UI, "LoadViewModel: Load not found with id: $loadId")
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            message = "Load not found: $loadId",
                            messageType = MessageType.ERROR,
                        )
                    return@launch
                }

                logger.info(LogCategory.UI, "LoadViewModel: Rejecting load ${load.loadName} (id: $loadId)")

                // Step 2: Reject load
                val result =
                    withContext(Dispatchers.Default) {
                        rejectLoadUseCase(loadId)
                    }

                if (result.isSuccess) {
                    logger.info(LogCategory.UI, "LoadViewModel: Successfully rejected load ${load.loadName} (id: $loadId)")
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            shouldNavigateBackAfterReject = true, // Устанавливаем флаг для навигации назад
                        )
                } else {
                    logger.error(
                        LogCategory.UI,
                        "LoadViewModel: Failed to reject load: ${result.exceptionOrNull()?.message}",
                    )
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            message = "Failed to reject load: ${result.exceptionOrNull()?.message}",
                            messageType = MessageType.ERROR,
                        )
                }
            } catch (e: Exception) {
                logger.error(LogCategory.UI, "LoadViewModel: Exception during reject load: ${e.message}")
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        message = "Error rejecting load: ${e.message}",
                        messageType = MessageType.ERROR,
                    )
            }
        }
    }

    fun confirmLoadDelivered() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showLoadDeliveredDialog = false,
                isLoading = true,
            )

            try {
                // Step 1: Find load by id
                val loads = loadRepository.getCachedLoads()
                val load = loads.find { it.id == loadId }

                if (load == null) {
                    logger.error(LogCategory.UI, "HomeViewModel: Load not found with id: $loadId")
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            message = "Load not found: $loadId",
                            messageType = MessageType.ERROR,
                        )
                    return@launch
                }

                logger.info(LogCategory.UI, "HomeViewModel: Marking load ${load.loadName} (id: $loadId) as delivered")

                // Step 2: Disconnect from load (marks as delivered)
                val disconnectResult =
                    withContext(Dispatchers.Default) {
                        disconnectFromLoadUseCase(loadId)
                    }
                if (disconnectResult.isFailure) {
                    logger.error(
                        LogCategory.UI,
                        "HomeViewModel: Failed to mark load as delivered: ${disconnectResult.exceptionOrNull()?.message}",
                    )
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            message = "Failed to mark load as delivered: ${disconnectResult.exceptionOrNull()?.message}",
                            messageType = MessageType.ERROR,
                        )
                    return@launch
                }

                logger.info(LogCategory.UI, "HomeViewModel: Successfully marked load ${load.loadName} (id: $loadId) as delivered")

                // Step 3: Stop tracking
                val result = stopTrackingUseCase()
                if (result.isFailure) {
                    logger.error(LogCategory.UI, "HomeViewModel: Failed to stop tracking: ${result.exceptionOrNull()?.message}")
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            message = "Load marked as delivered, but failed to stop tracking: ${result.exceptionOrNull()?.message}",
                            messageType = MessageType.ERROR,
                        )
                    return@launch
                }

                logger.info(LogCategory.UI, "HomeViewModel: Successfully stopped tracking after marking load as delivered")

                // Обновляем статус трекинга
                val trackingStatus = getTrackingStatusUseCase()
                _uiState.value =
                    _uiState.value.copy(
                        trackingStatus = trackingStatus,
                        message = "Load marked as delivered",
                        messageType = MessageType.SUCCESS,
                        shouldNavigateBack = true, // Устанавливаем флаг для навигации назад
                    )
            } catch (e: Exception) {
                logger.error(LogCategory.UI, "HomeViewModel: Exception during mark load as delivered: ${e.message}")
                _uiState.value =
                    _uiState.value.copy(
                        message = "Error marking load as delivered: ${e.message}",
                        messageType = MessageType.ERROR,
                    )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
