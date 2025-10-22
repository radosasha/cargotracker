package com.shiplocate.presentation.model

import com.shiplocate.domain.model.logs.LogFile

/**
 * Состояние экрана логов
 */
data class LogsUiState(
    val logFiles: List<LogFile> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val selectedCount: Int = 0,
    val message: String? = null,
    val messageType: MessageType? = null,
) {
    /**
     * Проверяет, можно ли отправить логи
     */
    val canSend: Boolean
        get() = selectedCount > 0 && !isSending

    /**
     * Проверяет, выбраны ли все файлы
     */
    val isAllSelected: Boolean
        get() = logFiles.isNotEmpty() && selectedCount == logFiles.size

    /**
     * Проверяет, есть ли выбранные файлы
     */
    val hasSelection: Boolean
        get() = selectedCount > 0
}
