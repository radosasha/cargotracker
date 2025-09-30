package com.tracker.presentation.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.domain.usecase.GetPermissionStatusUseCase
import com.tracker.domain.usecase.GetTrackingStatusUseCase
import com.tracker.domain.usecase.RequestAllPermissionsUseCase
import com.tracker.domain.usecase.StartTrackingUseCase
import com.tracker.domain.usecase.StopTrackingUseCase
import com.tracker.presentation.model.HomeUiState
import com.tracker.presentation.model.MessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для главного экрана
 */
class HomeViewModel(
    private val getPermissionStatusUseCase: GetPermissionStatusUseCase,
    private val getTrackingStatusUseCase: GetTrackingStatusUseCase,
    private val requestAllPermissionsUseCase: RequestAllPermissionsUseCase,
    private val startTrackingUseCase: StartTrackingUseCase,
    private val stopTrackingUseCase: StopTrackingUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        observeStatus()
    }
    
    private fun observeStatus() {
        viewModelScope.launch {
            try {
                val permissionStatus = getPermissionStatusUseCase()
                val trackingStatus = getTrackingStatusUseCase()
                
                _uiState.value = _uiState.value.copy(
                    permissionStatus = permissionStatus,
                    trackingStatus = trackingStatus,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Ошибка при загрузке статуса: ${e.message}",
                    messageType = MessageType.ERROR
                )
            }
        }
    }
    
    fun onRequestPermissions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = requestAllPermissionsUseCase()
                println("HomeViewModel.onRequestPermissions() - result: ${result.isSuccess}")
                if (result.isSuccess) {
                    val permissionStatus = result.getOrNull()
                    println("HomeViewModel.onRequestPermissions() - permissionStatus: $permissionStatus")
                    println("HomeViewModel.onRequestPermissions() - hasAllPermissions: ${permissionStatus?.hasAllPermissions}")
                    
                    _uiState.value = _uiState.value.copy(
                        permissionStatus = permissionStatus,
                        message = "Разрешения получены",
                        messageType = MessageType.SUCCESS
                    )
                    
                    // Если все разрешения получены, автоматически запускаем трекинг
                    if (permissionStatus?.hasAllPermissions == true) {
                        println("HomeViewModel.onRequestPermissions() - all permissions granted, starting tracking")
                        onStartTracking()
                    } else {
                        println("HomeViewModel.onRequestPermissions() - not all permissions granted, not starting tracking")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        message = "Не удалось получить разрешения: ${result.exceptionOrNull()?.message}",
                        messageType = MessageType.ERROR
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Ошибка при запросе разрешений: ${e.message}",
                    messageType = MessageType.ERROR
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun onStartTracking() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = startTrackingUseCase()
                if (result.isSuccess) {
                    // Обновляем статус трекинга
                    val trackingStatus = getTrackingStatusUseCase()
                    _uiState.value = _uiState.value.copy(
                        trackingStatus = trackingStatus,
                        message = "GPS трекинг запущен",
                        messageType = MessageType.SUCCESS
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        message = "Не удалось запустить трекинг: ${result.exceptionOrNull()?.message}",
                        messageType = MessageType.ERROR
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Ошибка при запуске трекинга: ${e.message}",
                    messageType = MessageType.ERROR
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun onStopTracking() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = stopTrackingUseCase()
                if (result.isSuccess) {
                    // Обновляем статус трекинга
                    val trackingStatus = getTrackingStatusUseCase()
                    _uiState.value = _uiState.value.copy(
                        trackingStatus = trackingStatus,
                        message = "GPS трекинг остановлен",
                        messageType = MessageType.SUCCESS
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        message = "Не удалось остановить трекинг: ${result.exceptionOrNull()?.message}",
                        messageType = MessageType.ERROR
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Ошибка при остановке трекинга: ${e.message}",
                    messageType = MessageType.ERROR
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            message = null,
            messageType = null
        )
    }
}
