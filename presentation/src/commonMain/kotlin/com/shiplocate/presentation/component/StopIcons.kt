package com.shiplocate.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Иконка для TYPE_PICKUP - точка где надо забрать груз
 * Иконка: стрелка вверх с коробкой
 */
@Composable
fun PickupIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val centerX = size.toPx() / 2
        val centerY = size.toPx() / 2
        val iconSize = size.toPx() * 0.6f

        // Коробка (прямоугольник)
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - iconSize * 0.3f, centerY - iconSize * 0.2f),
            size = androidx.compose.ui.geometry.Size(iconSize * 0.6f, iconSize * 0.4f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )

        // Стрелка вверх
        val arrowPath = Path().apply {
            moveTo(centerX, centerY - iconSize * 0.2f)
            lineTo(centerX - iconSize * 0.15f, centerY - iconSize * 0.05f)
            lineTo(centerX, centerY)
            lineTo(centerX + iconSize * 0.15f, centerY - iconSize * 0.05f)
            close()
        }
        drawPath(arrowPath, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

/**
 * Иконка для TYPE_BORDER - место пересечения границы стран
 * Иконка: флаг
 */
@Composable
fun BorderIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val centerX = size.toPx() / 2
        val centerY = size.toPx() / 2
        val iconSize = size.toPx() * 0.6f

        // Древко флага
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(centerX - iconSize * 0.3f, centerY - iconSize * 0.3f),
            end = androidx.compose.ui.geometry.Offset(centerX - iconSize * 0.3f, centerY + iconSize * 0.3f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )

        // Полотнище флага (треугольник)
        val flagPath = Path().apply {
            moveTo(centerX - iconSize * 0.3f, centerY - iconSize * 0.3f)
            lineTo(centerX + iconSize * 0.2f, centerY - iconSize * 0.1f)
            lineTo(centerX - iconSize * 0.3f, centerY + iconSize * 0.1f)
            close()
        }
        drawPath(flagPath, color = color)
    }
}

/**
 * Иконка для TYPE_DELIVERY - место куда надо везти груз
 * Иконка: стрелка вниз с коробкой
 */
@Composable
fun DeliveryIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val centerX = size.toPx() / 2
        val centerY = size.toPx() / 2
        val iconSize = size.toPx() * 0.6f

        // Коробка (прямоугольник)
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - iconSize * 0.3f, centerY - iconSize * 0.2f),
            size = androidx.compose.ui.geometry.Size(iconSize * 0.6f, iconSize * 0.4f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )

        // Стрелка вниз
        val arrowPath = Path().apply {
            moveTo(centerX, centerY + iconSize * 0.2f)
            lineTo(centerX - iconSize * 0.15f, centerY + iconSize * 0.05f)
            lineTo(centerX, centerY)
            lineTo(centerX + iconSize * 0.15f, centerY + iconSize * 0.05f)
            close()
        }
        drawPath(arrowPath, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

/**
 * Иконка для неизвестного типа стопа
 * Иконка: вопросительный знак в круге
 */
@Composable
fun UnknownStopIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val centerX = size.toPx() / 2
        val centerY = size.toPx() / 2
        val iconSize = size.toPx() * 0.5f

        // Вопросительный знак
        // Верхняя часть (круг)
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - iconSize * 0.2f, centerY - iconSize * 0.3f),
            size = androidx.compose.ui.geometry.Size(iconSize * 0.4f, iconSize * 0.3f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )

        // Вертикальная линия
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(centerX, centerY - iconSize * 0.1f),
            end = androidx.compose.ui.geometry.Offset(centerX, centerY + iconSize * 0.2f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )

        // Точка внизу
        drawCircle(
            color = color,
            radius = 2.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(centerX, centerY + iconSize * 0.25f),
        )
    }
}

/**
 * Иконка стопа в кружке с фоном
 */
@Composable
fun StopIconWithBackground(
    stopType: Int,
    backgroundColor: Color,
    iconColor: Color = Color.White,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        when (stopType) {
            StopType.TYPE_PICKUP -> PickupIcon(color = iconColor, size = size * 0.75f)
            StopType.TYPE_BORDER -> BorderIcon(color = iconColor, size = size * 0.75f)
            StopType.TYPE_DELIVERY -> DeliveryIcon(color = iconColor, size = size * 0.75f)
            else -> UnknownStopIcon(color = iconColor, size = size * 0.75f)
        }
    }
}

