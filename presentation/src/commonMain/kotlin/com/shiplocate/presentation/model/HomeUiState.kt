package com.shiplocate.presentation.model

import com.shiplocate.domain.model.PermissionStatus
import com.shiplocate.domain.model.TrackingStatus

/**
 * Presentation модель для состояния главного экрана
 */
data class HomeUiState(
    val loadId: String? = null,
    val permissionStatus: PermissionStatus? = null,
    val trackingStatus: TrackingStatus = TrackingStatus.STOPPED,
    val isLoading: Boolean = true,
    val message: String? = null,
    val messageType: MessageType? = null,
)

/**
 * Типы сообщений для UI
 */
enum class MessageType {
    SUCCESS,
    ERROR,
    INFO,
}
