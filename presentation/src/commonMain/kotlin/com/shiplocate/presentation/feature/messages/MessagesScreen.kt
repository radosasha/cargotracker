package com.shiplocate.presentation.feature.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shiplocate.domain.model.message.Message
import com.shiplocate.presentation.util.ClipboardManager
import com.shiplocate.presentation.util.DateFormatter
import com.shiplocate.presentation.util.rememberClipboardManager
import kotlinx.coroutines.delay

/**
 * Messages screen for displaying and sending messages
 */
@Suppress("FunctionName")
@Composable
fun MessagesScreen(
    paddingValues: PaddingValues,
    loadId: Long,
    viewModel: MessagesViewModel,
    onNavigateBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = rememberClipboardManager()
    var showToast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(loadId) {
        viewModel.initialize(loadId)
    }

    // Auto-scroll to bottom when new messages arrive
    val listState = rememberLazyListState()
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    MessageItem(
                        message = message,
                        onLongClick = {
                            clipboardManager.copyToClipboard(message.message)
                            showToast = "Message copied"
                        },
                    )
                }
            }

            // Input field and send button
            MessageInputBar(
                messageText = uiState.messageText,
                onMessageTextChanged = viewModel::onMessageTextChanged,
                onSendClick = viewModel::sendMessage,
                canSend = uiState.canSendMessage,
                isSending = uiState.isSending,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        // Toast message
        showToast?.let { toastMessage ->
            LaunchedEffect(toastMessage) {
                delay(2000)
                showToast = null
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp),
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = toastMessage,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageItem(
    message: Message,
    onLongClick: () -> Unit,
) {
    val isDriver = message.type == Message.MESSAGE_TYPE_DRIVER

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isDriver) 32.dp else 0.dp, vertical = 4.dp),
        contentAlignment = if (isDriver) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongClick() },
                    )
                },
            colors = CardDefaults.cardColors(
                containerColor = if (isDriver) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
            ) {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDriver) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = DateFormatter.formatMessageDateTime(message.datetime),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDriver) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        },
                    )

                    // Status indicator (only for driver messages)
                    if (isDriver) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MessageStatusIndicator(isSent = message.id != 0L)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageStatusIndicator(isSent: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = if (isSent) "Sent" else "Sending",
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
        )
        if (isSent) {
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Delivered",
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun MessageInputBar(
    messageText: String,
    onMessageTextChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    canSend: Boolean,
    isSending: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = messageText,
            onValueChange = onMessageTextChanged,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text("Type a message...")
            },
            enabled = !isSending,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            singleLine = false,
            maxLines = 4,
        )

        IconButton(
            onClick = onSendClick,
            enabled = canSend,
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (canSend) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    },
                )
            }
        }
    }
}

