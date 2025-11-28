package com.shiplocate.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shiplocate.domain.model.load.Stop
import com.shiplocate.presentation.util.DateFormatter

/**
 * Типы стопов
 */
object StopType {
    const val TYPE_PICKUP = 0
    const val TYPE_BORDER = 1
    const val TYPE_DELIVERY = 2
}

/**
 * Компонент для отображения списка стопов в формате timeline
 */
@Composable
fun StopsTimeline(
    stops: List<Stop>,
    modifier: Modifier = Modifier,
    showCompletionButtons: Boolean = false,
    onUpdateStopCompletion: ((Long, Int) -> Unit)? = null,
    isLoadingCompletion: Boolean = false,
) {
    if (stops.isEmpty()) {
        return
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp), // Убираем spacing между элементами
    ) {
        stops.sortedBy { it.index }.forEachIndexed { index, stop ->
            StopTimelineItem(
                stop = stop,
                showCompletionButton = showCompletionButtons,
                onUpdateStopCompletion = onUpdateStopCompletion,
                isLoadingCompletion = isLoadingCompletion,
            )
        }
    }
}

@Composable
private fun StopTimelineItem(
    stop: Stop,
    showCompletionButton: Boolean = false,
    onUpdateStopCompletion: ((Long, Int) -> Unit)? = null,
    isLoadingCompletion: Boolean = false,
) {
    val (backgroundColor, iconColor) = when (stop.type) {
        StopType.TYPE_PICKUP -> Color(0xFFFF9800) to Color.White // Orange background, white icon
        StopType.TYPE_BORDER -> Color(0xFF03A9F4) to Color.White // Light Blue background, white icon
        StopType.TYPE_DELIVERY -> Color(0xFF4CAF50) to Color.White // Green background, white icon
        else -> Color(0xFF9E9E9E) to Color.White // Gray background, white icon
    }

    val title = when (stop.type) {
        StopType.TYPE_PICKUP -> "Pickup"
        StopType.TYPE_BORDER -> "Stop"
        StopType.TYPE_DELIVERY -> "Delivery"
        else -> "Unknown"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        // Иконка
        Column(
            modifier = Modifier.width(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Иконка в кружке
            StopIconWithBackground(
                stopType = stop.type,
                backgroundColor = backgroundColor,
                iconColor = iconColor,
                modifier = Modifier.size(32.dp),
            )
        }

        // Текстовая информация
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Дата и адрес
            val dateText = if (stop.date > 0) {
                try {
                    DateFormatter.formatDateWithMonthName(stop.date)
                } catch (e: Exception) {
                    "Error parsing date"
                }
            } else {
                ""
            }

            val locationNameText = stop.locationName.ifEmpty { "" }
            val locationAddressText = stop.locationAddress.ifEmpty { "No address" }

            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (locationNameText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = locationNameText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = locationAddressText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Отображаем note, если оно не пустое
            val noteText = stop.note?.takeIf { it.isNotEmpty() }
            if (noteText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = noteText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Кнопка для обновления completion (только для upcoming loads)
            if (showCompletionButton && onUpdateStopCompletion != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val isCompleted = stop.completion == Stop.STOP_COMPLETION_COMPLETED
                Button(
                    onClick = {
                        val newCompletion = if (isCompleted) {
                            Stop.STOP_COMPLETION_NOT_COMPLETED
                        } else {
                            Stop.STOP_COMPLETION_COMPLETED
                        }
                        onUpdateStopCompletion(stop.id, newCompletion)
                    },
                    enabled = !isLoadingCompletion,
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (isCompleted) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                ) {
                    if (isLoadingCompletion) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(if (isCompleted) "Undo Completion" else "Mark Completed")
                    }
                }
            }
        }
    }
}

