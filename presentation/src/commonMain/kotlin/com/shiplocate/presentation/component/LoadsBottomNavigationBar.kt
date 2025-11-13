package com.shiplocate.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Bottom Navigation Bar for LoadsScreen
 * Displays Active and Upcoming tabs
 */
@Composable
fun LoadsBottomNavigationBar(
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
    ) {
        NavigationBarItem(
            selected = currentPage == 0,
            onClick = { onPageSelected(0) },
            icon = {
                ActiveIcon(
                    color = if (currentPage == 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    iconSize = 20.dp,
                )
            },
            label = {
                Text(
                    text = "Active",
                    style = MaterialTheme.typography.labelSmall,
                )
            },
        )
        NavigationBarItem(
            selected = currentPage == 1,
            onClick = { onPageSelected(1) },
            icon = {
                UpcomingIcon(
                    color = if (currentPage == 1) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    iconSize = 20.dp,
                )
            },
            label = {
                Text(
                    text = "Upcoming",
                    style = MaterialTheme.typography.labelSmall,
                )
            },
        )
    }
}

/**
 * Иконка для Active loads (активные/подключенные)
 * Иконка: круг с галочкой внутри (символ активности/подключения)
 */
@Composable
private fun ActiveIcon(
    color: Color,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
) {
    Canvas(modifier = modifier.size(iconSize)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val iconSizePx = min(size.width, size.height) * 0.8f

        // Круг
        drawCircle(
            color = color,
            radius = iconSizePx / 2f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )

        // Галочка
        val checkPath = Path().apply {
            moveTo(centerX - iconSizePx * 0.2f, centerY)
            lineTo(centerX - iconSizePx * 0.05f, centerY + iconSizePx * 0.15f)
            lineTo(centerX + iconSizePx * 0.2f, centerY - iconSizePx * 0.15f)
        }
        drawPath(
            checkPath,
            color = color,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

/**
 * Иконка для Upcoming loads (предстоящие)
 * Иконка: календарь (символ предстоящих событий)
 */
@Composable
private fun UpcomingIcon(
    color: Color,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
) {
    Canvas(modifier = modifier.size(iconSize)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val iconSizePx = min(size.width, size.height) * 0.7f

        // Календарь - прямоугольник
        drawRect(
            color = color,
            topLeft = Offset(centerX - iconSizePx * 0.35f, centerY - iconSizePx * 0.3f),
            size = Size(iconSizePx * 0.7f, iconSizePx * 0.6f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )

        // Верхняя часть календаря (заголовок)
        drawRect(
            color = color,
            topLeft = Offset(centerX - iconSizePx * 0.35f, centerY - iconSizePx * 0.3f),
            size = Size(iconSizePx * 0.7f, iconSizePx * 0.15f),
        )

        // Кольца для переплета
        val ringRadius = iconSizePx * 0.08f
        drawCircle(
            color = color,
            radius = ringRadius,
            center = Offset(centerX - iconSizePx * 0.2f, centerY - iconSizePx * 0.225f),
        )
        drawCircle(
            color = color,
            radius = ringRadius,
            center = Offset(centerX + iconSizePx * 0.2f, centerY - iconSizePx * 0.225f),
        )

        // Линии для дней недели
        val lineY1 = centerY - iconSizePx * 0.1f
        val lineY2 = centerY + iconSizePx * 0.1f
        val lineSpacing = iconSizePx * 0.15f

        // Три горизонтальные линии
        for (i in 0..2) {
            val y = lineY1 + i * lineSpacing
            drawLine(
                color = color,
                start = Offset(centerX - iconSizePx * 0.25f, y),
                end = Offset(centerX + iconSizePx * 0.25f, y),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

