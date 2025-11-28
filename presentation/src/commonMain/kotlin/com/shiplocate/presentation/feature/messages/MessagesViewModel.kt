package com.shiplocate.presentation.feature.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.message.Message
import com.shiplocate.domain.usecase.message.FetchMessagesUseCase
import com.shiplocate.domain.usecase.message.ObserveMessagesUseCase
import com.shiplocate.domain.usecase.message.SendMessageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * ViewModel for Messages screen
 */
class MessagesViewModel(
    private val observeMessagesUseCase: ObserveMessagesUseCase,
    private val fetchMessagesUseCase: FetchMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val logger: Logger,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    private var loadId: Long = 0L
    private var currentMessageText: String = ""

    fun initialize(loadId: Long) {
        logger.info(LogCategory.UI, "MessagesViewModel: Initialized with loadId = $loadId")
        this.loadId = loadId

        // Load cached messages first
        viewModelScope.launch(Dispatchers.Default) {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Get messages Flow from use case
                val messagesFlow = observeMessagesUseCase(loadId)
                if (messagesFlow == null) {
                    logger.warn(LogCategory.UI, "MessagesViewModel: Not authenticated")
                    _uiState.value = _uiState.value.copy(
                        error = "Not authenticated",
                        isLoading = false,
                    )
                    return@launch
                }

                // Observe messages from local database
                messagesFlow.onEach { messages ->
                    logger.debug(LogCategory.UI, "MessagesViewModel: Received ${messages.size} messages from local DB")
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        isLoading = false,
                    )
                }.launchIn(viewModelScope)

                // Refresh messages from server
                refreshMessages(loadId)
            } catch (e: Exception) {
                logger.error(LogCategory.UI, "MessagesViewModel: Error initializing: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message,
                )
            }
        }
    }

    private suspend fun refreshMessages(loadId: Long) {
        try {
            logger.info(LogCategory.UI, "MessagesViewModel: Refreshing messages from server")
            val result = fetchMessagesUseCase(loadId)

            if (result.isFailure) {
                logger.warn(LogCategory.UI, "MessagesViewModel: Failed to refresh messages: ${result.exceptionOrNull()?.message}")
                // Don't show error, just use cached messages
            } else {
                logger.info(LogCategory.UI, "MessagesViewModel: Successfully refreshed messages")
            }
        } catch (e: Exception) {
            logger.error(LogCategory.UI, "MessagesViewModel: Error refreshing messages: ${e.message}", e)
            // Don't show error, just use cached messages
        }
    }

    fun onMessageTextChanged(text: String) {
        currentMessageText = text
        val hasNonWhitespaceChars = text.trim().isNotEmpty()
        _uiState.value = _uiState.value.copy(
            messageText = text,
            canSendMessage = hasNonWhitespaceChars && !_uiState.value.isSending,
        )
    }

    fun sendMessage() {
        val messageText = currentMessageText.trim()
        if (messageText.isEmpty() || _uiState.value.isSending) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSending = true, canSendMessage = false)

                // First, save temporary message to database with id = 0 (single checkmark)
                val tempMessage = Message(
                    id = 0, // Temporary, will be updated when server responds
                    loadId = loadId,
                    message = messageText,
                    type = Message.MESSAGE_TYPE_DRIVER,
                    datetime = Clock.System.now().toEpochMilliseconds(),
                )

                // Now send to server
                val result = sendMessageUseCase(loadId, tempMessage)

                if (result.isSuccess) {
                    val sentMessage = result.getOrNull()!!
                    logger.info(LogCategory.UI, "MessagesViewModel: Message sent successfully, updating with server response")

                    // Update message with server response (double checkmark)
//                    updateMessageUseCase(sentMessage)

                    // Clear input
                    currentMessageText = ""
                    _uiState.value = _uiState.value.copy(
                        messageText = "",
                        isSending = false,
                        canSendMessage = false,
                    )
                } else {
                    logger.error(LogCategory.UI, "MessagesViewModel: Failed to send message: ${result.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to send message",
                    )
                }
            } catch (e: Exception) {
                logger.error(LogCategory.UI, "MessagesViewModel: Exception sending message: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    error = e.message ?: "Failed to send message",
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI State for Messages screen
 */
data class MessagesUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val messageText: String = "",
    val canSendMessage: Boolean = false,
    val error: String? = null,
)

