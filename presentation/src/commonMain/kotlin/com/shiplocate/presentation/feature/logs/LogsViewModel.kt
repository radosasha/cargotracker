package com.shiplocate.presentation.feature.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.repository.LogsRepository
import com.shiplocate.domain.usecase.logs.GetLogsClientIdUseCase
import com.shiplocate.domain.usecase.logs.GetLogsUseCase
import com.shiplocate.domain.usecase.logs.SendLogsUseCase
import com.shiplocate.presentation.model.LogsUiState
import com.shiplocate.presentation.model.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel для экрана логов
 */
class LogsViewModel(
    private val getLogsUseCase: GetLogsUseCase,
    private val sendLogsUseCase: SendLogsUseCase,
    private val getLogsClientIdUseCase: GetLogsClientIdUseCase,
    private val logsRepository: LogsRepository,
    private val logger: Logger,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    init {
        loadLogFiles()
    }

    /**
     * Загружает список лог-файлов
     */
    fun loadLogFiles() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, message = null)
                logger.debug(LogCategory.GENERAL, "LogsViewModel: Loading log files")

                val files = withContext(Dispatchers.Default) {
                    getLogsUseCase()
                }

                _uiState.value =
                    _uiState.value.copy(
                        logFiles = files,
                        isLoading = false,
                        selectedCount = files.count { it.isSelected },
                    )

                logger.info(LogCategory.GENERAL, "LogsViewModel: Loaded ${files.size} log files")
            } catch (e: Exception) {
                logger.error(LogCategory.GENERAL, "LogsViewModel: Error loading log files: ${e.message}", e)
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        message = "Failed to upload files: ${e.message}",
                        messageType = MessageType.ERROR,
                    )
            }
        }
    }

    /**
     * Переключает выбор файла
     */
    fun toggleFileSelection(fileName: String) {
        val currentState = _uiState.value
        val updatedFiles =
            currentState.logFiles.map { file ->
                if (file.name == fileName) {
                    file.copy(isSelected = !file.isSelected)
                } else {
                    file
                }
            }

        val selectedCount = updatedFiles.count { it.isSelected }

        _uiState.value =
            currentState.copy(
                logFiles = updatedFiles,
                selectedCount = selectedCount,
            )

        logger.debug(LogCategory.GENERAL, "LogsViewModel: Toggled selection for $fileName, selected: $selectedCount")
    }

    /**
     * Выбирает все файлы
     */
    fun selectAllFiles() {
        val currentState = _uiState.value
        val updatedFiles = currentState.logFiles.map { it.copy(isSelected = true) }

        _uiState.value =
            currentState.copy(
                logFiles = updatedFiles,
                selectedCount = updatedFiles.size,
            )

        logger.debug(LogCategory.GENERAL, "LogsViewModel: Selected all ${updatedFiles.size} files")
    }

    /**
     * Снимает выбор со всех файлов
     */
    fun unselectAllFiles() {
        val currentState = _uiState.value
        val updatedFiles = currentState.logFiles.map { it.copy(isSelected = false) }

        _uiState.value =
            currentState.copy(
                logFiles = updatedFiles,
                selectedCount = 0,
            )

        logger.debug(LogCategory.GENERAL, "LogsViewModel: Unselected all files")
    }

    /**
     * Отправляет выбранные файлы
     */
    fun sendSelectedFiles() {
        val currentState = _uiState.value
        val selectedFiles = currentState.logFiles.filter { it.isSelected }

        if (selectedFiles.isEmpty()) {
            logger.warn(LogCategory.GENERAL, "LogsViewModel: No files selected for sending")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value =
                    currentState.copy(
                        isSending = true,
                        message = null,
                    )

                logger.info(LogCategory.GENERAL, "LogsViewModel: Sending ${selectedFiles.size} log files")

                val result = withContext(Dispatchers.Default) {
                    sendLogsUseCase(clientId = getLogsClientIdUseCase(), files = selectedFiles)
                }

                if (result.isSuccess) {
                    // Обновляем список файлов после успешной отправки
                    loadLogFiles()
                    _uiState.value =
                        _uiState.value.copy(
                            isSending = false,
                            message = "Sent successfully ${selectedFiles.size} files",
                            messageType = MessageType.SUCCESS,
                        )
                    logger.info(LogCategory.GENERAL, "LogsViewModel: Successfully sent ${selectedFiles.size} log files")
                } else {
                    _uiState.value =
                        _uiState.value.copy(
                            isSending = false,
                            message = "Failed to send: ${result.exceptionOrNull()?.message}",
                            messageType = MessageType.ERROR,
                        )
                    logger.error(LogCategory.GENERAL, "LogsViewModel: Failed to send log files: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                logger.error(LogCategory.GENERAL, "LogsViewModel: Error sending log files: ${e.message}", e)
                _uiState.value =
                    _uiState.value.copy(
                        isSending = false,
                        message = "Failed to send: ${e.message}",
                        messageType = MessageType.ERROR,
                    )
            }
        }
    }

    /**
     * Удаляет выбранные файлы
     */
    fun deleteSelectedFiles() {
        val currentState = _uiState.value
        val selectedFiles = currentState.logFiles.filter { it.isSelected }

        if (selectedFiles.isEmpty()) {
            logger.warn(LogCategory.GENERAL, "LogsViewModel: No files selected for deletion")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = currentState.copy(isSending = true, message = null)

                logger.info(LogCategory.GENERAL, "LogsViewModel: Deleting ${selectedFiles.size} log files")

                var deletedCount = 0
                selectedFiles.forEach { file ->
                    try {
                        val result = withContext(Dispatchers.Default) {
                            logsRepository.deleteLogFile(file.name)
                        }
                        if (result) {
                            deletedCount++
                        }
                    } catch (e: Exception) {
                        logger.error(LogCategory.GENERAL, "LogsViewModel: Failed to delete file ${file.name}: ${e.message}")
                    }
                }

                if (deletedCount > 0) {
                    // Обновляем список файлов после удаления
                    loadLogFiles()
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        message = "Deleted $deletedCount files",
                        messageType = MessageType.SUCCESS,
                    )
                    logger.info(LogCategory.GENERAL, "LogsViewModel: Successfully deleted $deletedCount log files")
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        message = "Failed to delete files",
                        messageType = MessageType.ERROR,
                    )
                }
            } catch (e: Exception) {
                logger.error(LogCategory.GENERAL, "LogsViewModel: Error deleting log files: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    message = "Failed to delete: ${e.message}",
                    messageType = MessageType.ERROR,
                )
            }
        }
    }

    /**
     * Очищает сообщение
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, messageType = null)
    }
}



