package com.shiplocate.presentation.model

import com.shiplocate.domain.model.PermissionStatus

/**
 * Presentation модель для состояния главного экрана
 */
data class LoadUiState(
    val load: LoadUiModel? = null,
    val permissionStatus: PermissionStatus? = null,
    val hasActiveLoad: Boolean = false,
    val isLoading: Boolean = true,
    val message: String? = null,
    val messageType: MessageType? = null,
    val showLoadDeliveredDialog: Boolean = false,
    val shouldNavigateBack: Boolean = false,
    val showRejectLoadDialog: Boolean = false,
    val shouldNavigateBackAfterReject: Boolean = false,
    val shouldNavigateBackAfterStart: Boolean = false,
    val showActiveLoadDialog: Boolean = false,
)

/**
 * Типы сообщений для UI
 */
enum class MessageType {
    SUCCESS,
    ERROR,
    INFO,
}
