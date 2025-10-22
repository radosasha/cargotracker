package com.shiplocate.presentation.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.usecase.GetPermissionStatusUseCase
import com.shiplocate.domain.usecase.GetTrackingStatusUseCase
import com.shiplocate.domain.usecase.RequestAllPermissionsUseCase
import com.shiplocate.domain.usecase.StartTrackingUseCase
import com.shiplocate.domain.usecase.StopTrackingUseCase
import com.shiplocate.domain.usecase.TestServerUseCase
import com.shiplocate.domain.usecase.load.ConnectToLoadUseCase
import com.shiplocate.domain.usecase.load.DisconnectFromLoadUseCase
import com.shiplocate.presentation.model.HomeUiState
import com.shiplocate.presentation.model.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel для главного экрана
 */
class HomeViewModel(
    private val getPermissionStatusUseCase: GetPermissionStatusUseCase,
    private val getTrackingStatusUseCase: GetTrackingStatusUseCase,
    private val requestAllPermissionsUseCase: RequestAllPermissionsUseCase,
    private val startTrackingUseCase: StartTrackingUseCase,
    private val stopTrackingUseCase: StopTrackingUseCase,
    private val testServerUseCase: TestServerUseCase,
    private val connectToLoadUseCase: ConnectToLoadUseCase,
    private val disconnectFromLoadUseCase: DisconnectFromLoadUseCase,
    private val logger: Logger,
) : ViewModel() {

    private lateinit var loadId: String
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observePermissionsAndTrackingStatus()
    }

    fun initialize(id: String) {
        logger.info(LogCategory.UI, "HomeViewModel: Initialized with loadId = $id")

        loadId = id
        // Set loadId in uiState
        _uiState.value = _uiState.value.copy(loadId = id)
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

    fun requestPermissions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val result = requestAllPermissionsUseCase()
                logger.info(LogCategory.PERMISSIONS, "HomeViewModel.onRequestPermissions() - result: ${result.isSuccess}")
                if (result.isSuccess) {
                    val permissionStatus = result.getOrNull()
                    logger.debug(LogCategory.PERMISSIONS, "HomeViewModel.onRequestPermissions() - permissionStatus: $permissionStatus")
                    logger.debug(LogCategory.PERMISSIONS, "HomeViewModel.onRequestPermissions() - hasAllPermissions: ${permissionStatus?.hasAllPermissions}")

                    _uiState.value =
                        _uiState.value.copy(
                            permissionStatus = permissionStatus,
                            message = "Разрешения получены",
                            messageType = MessageType.SUCCESS,
                        )

                    // Если все разрешения получены, автоматически запускаем трекинг
                    if (permissionStatus?.hasAllPermissions == true) {
                        logger.info(LogCategory.PERMISSIONS, "HomeViewModel.onRequestPermissions() - all permissions granted, starting tracking")
                        startTracking()
                    } else {
                        logger.warn(LogCategory.PERMISSIONS, "HomeViewModel.onRequestPermissions() - not all permissions granted, not starting tracking")
                    }
                } else {
                    _uiState.value =
                        _uiState.value.copy(
                            message = "Не удалось получить разрешения: ${result.exceptionOrNull()?.message}",
                            messageType = MessageType.ERROR,
                        )
                }
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        message = "Ошибка при запросе разрешений: ${e.message}",
                        messageType = MessageType.ERROR,
                    )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun startTracking() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Step 1: Connect to load
                val currentLoadId = loadId

                logger.info(LogCategory.UI, "HomeViewModel: Connecting to load $currentLoadId before starting tracking")

                val connectResult =
                    withContext(Dispatchers.Default) {
                        connectToLoadUseCase(currentLoadId)
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

                logger.info(LogCategory.UI, "HomeViewModel: Successfully connected to load $currentLoadId")

                // Step 2: Start tracking
                val result = startTrackingUseCase()
                if (result.isSuccess) {
                    // Обновляем статус трекинга
                    val trackingStatus = getTrackingStatusUseCase()
                    _uiState.value =
                        _uiState.value.copy(
                            trackingStatus = trackingStatus,
                            message = "GPS трекинг запущен",
                            messageType = MessageType.SUCCESS,
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

    fun stopTracking() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Step 1: Disconnect from load
                val currentLoadId = loadId

                logger.info(LogCategory.UI, "HomeViewModel: Disconnecting from load $currentLoadId before stopping tracking")

                val disconnectResult =
                    withContext(Dispatchers.Default) {
                        disconnectFromLoadUseCase(currentLoadId)
                    }
                if (disconnectResult.isFailure) {
                    logger.error(LogCategory.UI, "HomeViewModel: Failed to disconnect from load: ${disconnectResult.exceptionOrNull()?.message}")
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            message = "Failed to disconnect from load: ${disconnectResult.exceptionOrNull()?.message}",
                            messageType = MessageType.ERROR,
                        )
                    return@launch
                }

                logger.info(LogCategory.UI, "HomeViewModel: Successfully disconnected from load $currentLoadId")

                // Step 2: Stop tracking
                val result = stopTrackingUseCase()
                if (result.isFailure) {
                    logger.error(LogCategory.UI, "HomeViewModel: Failed to stop tracking: ${result.exceptionOrNull()?.message}")
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            message = "Не удалось остановить трекинг: ${result.exceptionOrNull()?.message}",
                            messageType = MessageType.ERROR,
                        )
                    return@launch
                }

                logger.info(LogCategory.UI, "HomeViewModel: Successfully stopped tracking")

                // Обновляем статус трекинга
                val trackingStatus = getTrackingStatusUseCase()
                _uiState.value =
                    _uiState.value.copy(
                        trackingStatus = trackingStatus,
                        message = "GPS трекинг остановлен",
                        messageType = MessageType.SUCCESS,
                    )
            } catch (e: Exception) {
                logger.error(LogCategory.UI, "HomeViewModel: Exception during stop tracking: ${e.message}")
                _uiState.value =
                    _uiState.value.copy(
                        message = "Ошибка при остановке трекинга: ${e.message}",
                        messageType = MessageType.ERROR,
                    )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onTestServer() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                logger.info(LogCategory.NETWORK, "HomeViewModel.onTestServer() - testing server connection")
                val result = testServerUseCase()

                if (result.isSuccess) {
                    _uiState.value =
                        _uiState.value.copy(
                            message = "Тестовые координаты отправлены на сервер: 55.7558, 37.6176 (Москва)",
                            messageType = MessageType.SUCCESS,
                        )
                } else {
                    _uiState.value =
                        _uiState.value.copy(
                            message = "Ошибка при отправке тестовых координат: ${result.exceptionOrNull()?.message}",
                            messageType = MessageType.ERROR,
                        )
                }
            } catch (e: Exception) {
                logger.error(LogCategory.NETWORK, "HomeViewModel.onTestServer() - error: ${e.message}")
                _uiState.value =
                    _uiState.value.copy(
                        message = "Ошибка при тестировании сервера: ${e.message}",
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
}
