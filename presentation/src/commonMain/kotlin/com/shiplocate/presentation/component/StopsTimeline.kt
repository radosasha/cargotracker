package com.shiplocate.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
) {
    if (stops.isEmpty()) {
        return
    }

    Column(modifier = modifier) {
        stops.sortedBy { it.index }.forEachIndexed { index, stop ->
            StopTimelineItem(
                stop = stop,
                isLast = index == stops.size - 1,
            )
        }
    }
}

@Composable
private fun StopTimelineItem(
    stop: Stop,
    isLast: Boolean,
) {
    val (backgroundColor, iconColor) = when (stop.type) {
        StopType.TYPE_PICKUP -> Color(0xFFFF9800) to Color.White // Orange background, white icon
        StopType.TYPE_BORDER -> Color(0xFF03A9F4) to Color.White // Light Blue background, white icon
        StopType.TYPE_DELIVERY -> Color(0xFF4CAF50) to Color.White // Green background, white icon
        else -> Color(0xFF9E9E9E) to Color.White // Gray background, white icon
    }

    val title = when (stop.type) {
        StopType.TYPE_PICKUP -> "Pickup"
        StopType.TYPE_BORDER -> "Border"
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
        // Иконка и линия
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

            // Вертикальная линия (если не последний элемент)
            if (!isLast) {
                Spacer(modifier = Modifier.height(4.dp))
                Canvas(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp),
                ) {
                    drawLine(
                        color = backgroundColor.copy(alpha = 0.5f),
                        start = androidx.compose.ui.geometry.Offset(size.width / 2, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height),
                        strokeWidth = 2.dp.toPx(),
                    )
                }
            }
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

            val locationText = stop.locationAddress.ifEmpty { "No address" }

            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = locationText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

