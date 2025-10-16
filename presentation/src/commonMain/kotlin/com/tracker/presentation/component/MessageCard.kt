package com.tracker.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tracker.presentation.model.MessageType

/**
 * Компонент для отображения сообщений
 */
@Composable
fun MessageCard(
    message: String,
    messageType: MessageType?,
    onDismiss: () -> Unit,
) {
    val backgroundColor =
        when (messageType) {
            MessageType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
            MessageType.ERROR -> MaterialTheme.colorScheme.errorContainer
            MessageType.INFO -> MaterialTheme.colorScheme.secondaryContainer
            null -> MaterialTheme.colorScheme.surfaceVariant
        }

    val contentColor =
        when (messageType) {
            MessageType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
            MessageType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
            MessageType.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
            null -> MaterialTheme.colorScheme.onSurfaceVariant
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = backgroundColor,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )

            IconButton(onClick = onDismiss) {
                Text(
                    text = "✕",
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
