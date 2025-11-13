package com.shiplocate.presentation.model

import com.shiplocate.domain.model.PermissionStatus
import com.shiplocate.domain.model.TrackingStatus
import com.shiplocate.domain.model.load.Load

/**
 * Presentation модель для состояния главного экрана
 */
data class LoadUiState(
    val load: Load? = null,
    val permissionStatus: PermissionStatus? = null,
    val trackingStatus: TrackingStatus = TrackingStatus.STOPPED,
    val isLoading: Boolean = true,
    val message: String? = null,
    val messageType: MessageType? = null,
    val showLoadDeliveredDialog: Boolean = false,
    val shouldNavigateBack: Boolean = false,
)

/**
 * Типы сообщений для UI
 */
enum class MessageType {
    SUCCESS,
    ERROR,
    INFO,
}
