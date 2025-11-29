package com.shiplocate.presentation.feature.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.usecase.GetPermissionStatusUseCase
import com.shiplocate.domain.usecase.ObservePermissionsUseCase
import com.shiplocate.domain.usecase.RequestBackgroundLocationPermissionUseCase
import com.shiplocate.domain.usecase.RequestBatteryOptimizationDisableUseCase
import com.shiplocate.domain.usecase.RequestEnableHighAccuracyCase
import com.shiplocate.domain.usecase.RequestLocationPermissionUseCase
import com.shiplocate.domain.usecase.RequestNotificationPermissionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана разрешений
 */
class PermissionsViewModel(
    private val getPermissionStatusUseCase: GetPermissionStatusUseCase,
    private val observePermissionsUseCase: ObservePermissionsUseCase,
    private val requestLocationPermissionUseCase: RequestLocationPermissionUseCase,
    private val requestBackgroundLocationPermissionUseCase: RequestBackgroundLocationPermissionUseCase,
    private val requestBatteryOptimizationDisableUseCase: RequestBatteryOptimizationDisableUseCase,
    private val requestNotificationPermissionUseCase: RequestNotificationPermissionUseCase,
    private val requestEnableHighAccuracyUseCase: RequestEnableHighAccuracyCase,
    private val logger: Logger,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    init {
        refreshPermissionStatus()
        // Подписываемся на изменения разрешений из Flow
        observePermissionsUseCase()
            .onEach { status ->
                logger.debug(LogCategory.PERMISSIONS, "PermissionsViewModel: Received permission status update from Flow")
                updatePermissionStatus(status)
            }
            .launchIn(viewModelScope)
    }

    fun refreshPermissionStatus() {
        viewModelScope.launch {
            try {
                val status = getPermissionStatusUseCase()
                updatePermissionStatus(status)
            } catch (e: Exception) {
                logger.error(LogCategory.PERMISSIONS, "PermissionsViewModel: Error refreshing permission status: ${e.message}", e)
            }
        }
    }

    private fun updatePermissionStatus(status: com.shiplocate.domain.model.PermissionStatus) {
        _uiState.value =
            _uiState.value.copy(
                hasLocationPermission = status.hasLocationPermission,
                hasBackgroundLocationPermission = status.hasBackgroundLocationPermission,
                isBatteryOptimizationDisabled = status.isBatteryOptimizationDisabled,
                hasNotificationPermission = status.hasNotificationPermission,
                isLocationEnabled = status.isHighAccuracyEnabled,
                hasAllPermissions = status.hasAllPermissionsForTracking && status.hasNotificationPermission,
            )
    }

    fun requestLocationPermission() {
        viewModelScope.launch {
            try {
                logger.info(LogCategory.PERMISSIONS, "PermissionsViewModel: Requesting location permission")
                val result = requestLocationPermissionUseCase()
                if (result.isSuccess) {
                    val status = result.getOrNull()
                    _uiState.value =
                        _uiState.value.copy(
                            hasLocationPermission = status?.hasLocationPermission ?: false,
                        )
                    logger.info(LogCategory.PERMISSIONS, "PermissionsViewModel: Location permission granted")
                } else {
                    logger.warn(LogCategory.PERMISSIONS, "PermissionsViewModel: Location permission denied")
                }
                // Обновляем статус после запроса
                refreshPermissionStatus()
            } catch (e: Exception) {
                logger.error(LogCategory.PERMISSIONS, "PermissionsViewModel: Error requesting location permission: ${e.message}", e)
                refreshPermissionStatus()
            }
        }
    }

    fun requestBackgroundLocationPermission() {
        viewModelScope.launch {
            try {
                logger.info(LogCategory.PERMISSIONS, "PermissionsViewModel: Requesting background location permission")
                val result = requestBackgroundLocationPermissionUseCase()
                if (result.isSuccess) {
                    val status = result.getOrNull()
                    _uiState.value =
                        _uiState.value.copy(
                            hasBackgroundLocationPermission = status?.hasBackgroundLocationPermission ?: false,
                        )
                    logger.info(LogCategory.PERMISSIONS, "PermissionsViewModel: Background location permission granted")
                } else {
                    logger.warn(LogCategory.PERMISSIONS, "PermissionsViewModel: Background location permission denied")
                }
                // Обновляем статус после запроса
                refreshPermissionStatus()
            } catch (e: Exception) {
                logger.error(LogCategory.PERMISSIONS, "PermissionsViewModel: Error requesting background location permission: ${e.message}", e)
                refreshPermissionStatus()
            }
        }
    }

    fun requestBatteryOptimizationDisable() {
        viewModelScope.launch {
            try {
                logger.info(LogCategory.PERMISSIONS, "PermissionsViewModel: Requesting battery optimization disable")
                val result = requestBatteryOptimizationDisableUseCase()
                if (result.isSuccess) {
                    val status = result.getOrNull()
                    _uiState.value =
                        _uiState.value.copy(
                            isBatteryOptimizationDisabled = status?.isBatteryOptimizationDisabled ?: false,
                        )
                    logger.info(LogCategory.PERMISSIONS, "PermissionsViewModel: Battery optimization disabled")
                } else {
                    logger.warn(LogCategory.PERMISSIONS, "PermissionsViewModel: Battery optimization not disabled")
                }
                // Обновляем статус после запроса
                refreshPermissionStatus()
            } catch (e: Exception) {
                logger.error(LogCategory.PERMISSIONS, "PermissionsViewModel: Error requesting battery optimization disable: ${e.message}", e)
                refreshPermissionStatus()
            }
        }
    }

    fun requestNotificationPermission() {
        viewModelScope.launch {
            try {
                logger.info(LogCategory.PERMISSIONS, "PermissionsViewModel: Requesting notification permission")
                val result = requestNotificationPermissionUseCase()
                if (result.isSuccess) {
                    val granted = result.getOrNull() ?: false
                    _uiState.value =
                        _uiState.value.copy(
                            hasNotificationPermission = granted,
                        )
                    logger.info(LogCategory.PERMISSIONS, "PermissionsViewModel: Notification permission granted: $granted")
                } else {
                    logger.warn(LogCategory.PERMISSIONS, "PermissionsViewModel: Notification permission denied")
                }
                // Обновляем статус после запроса
                refreshPermissionStatus()
            } catch (e: Exception) {
                logger.error(LogCategory.PERMISSIONS, "PermissionsViewModel: Error requesting notification permission: ${e.message}", e)
                refreshPermissionStatus()
            }
        }
    }

    fun requestEnableHighAccuracy() {
        viewModelScope.launch {
            try {
                logger.info(LogCategory.PERMISSIONS, "PermissionsViewModel: Requesting enable GPS")
                val result = requestEnableHighAccuracyUseCase()
                if (result.isSuccess) {
                    val status = result.getOrNull()
                    _uiState.value =
                        _uiState.value.copy(
                            isLocationEnabled = status?.isHighAccuracyEnabled ?: _uiState.value.isLocationEnabled,
                        )
                    logger.info(LogCategory.PERMISSIONS, "PermissionsViewModel: GPS enable flow triggered")
                } else {
                    logger.warn(LogCategory.PERMISSIONS, "PermissionsViewModel: GPS enable request failed")
                }
                refreshPermissionStatus()
            } catch (e: Exception) {
                logger.error(LogCategory.PERMISSIONS, "PermissionsViewModel: Error requesting enable GPS: ${e.message}", e)
                refreshPermissionStatus()
            }
        }
    }
}

/**
 * UI State для экрана разрешений
 */
data class PermissionsUiState(
    val hasLocationPermission: Boolean = false,
    val hasBackgroundLocationPermission: Boolean = false,
    val isBatteryOptimizationDisabled: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val hasAllPermissions: Boolean = false,
)

