package com.shiplocate.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shiplocate.presentation.navigation.Screen

/**
 * Динамический TopAppBar для приложения
 * Обновляет заголовок и действия в зависимости от текущего экрана
 * Скрывается на auth экранах (ENTER_PHONE, ENTER_PIN)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerTopAppBar(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier,
    onLongPressTitle: (() -> Unit)? = null,
    onLogoutClick: (() -> Unit)? = null,
) {
    // Скрываем TopAppBar на auth экранах
    val shouldShowTopBar = when {
        currentRoute == Screen.ENTER_PHONE -> false
        currentRoute?.startsWith(Screen.ENTER_PIN) == true -> false
        else -> true
    }

    if (!shouldShowTopBar) {
        return
    }

    // Определяем заголовок в зависимости от текущего маршрута
    val title = when {
        currentRoute?.startsWith(Screen.LOAD) == true -> "Manage load"
        currentRoute == Screen.LOADS -> "ShipLocate"
        currentRoute == Screen.LOGS -> "Logs"
        else -> "ShipLocate"
    }

    // Показываем кнопку назад, если это не стартовый экран
    // В KMP Navigation previousBackStackEntry может работать нестабильно
    val canNavigateBack = when {
        currentRoute == Screen.LOADS -> false // Если это стартовый экран после авторизации
        else -> true
    }

    // Показываем кнопку logout только на экране Loads
    val showLogoutButton = currentRoute == Screen.LOADS && onLogoutClick != null

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Text(
                        text = "←",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        },
        actions = {
            if (showLogoutButton) {
                IconButton(onClick = { onLogoutClick?.invoke() }) {
                    LogoutIcon(
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        modifier =
            if (onLongPressTitle != null) {
                modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongPressTitle() },
                    )
                }
            } else {
                modifier
            },
    )
}

/**
 * Иконка logout - стрелка, выходящая из двери/прямоугольника
 */
@Composable
private fun LogoutIcon(
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val iconSize = size.width * 0.7f
        val strokeWidth = 2.dp.toPx()

        // Прямоугольник (дверь)
        val doorWidth = iconSize * 0.5f
        val doorHeight = iconSize * 0.6f
        val doorLeft = centerX - doorWidth / 2
        val doorTop = centerY - doorHeight / 2

        drawRect(
            color = color,
            topLeft = Offset(doorLeft, doorTop),
            size = Size(doorWidth, doorHeight),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )

        // Стрелка вправо (выход)
        val arrowStartX = doorLeft + doorWidth
        val arrowY = centerY
        val arrowLength = iconSize * 0.3f
        val arrowHeadSize = iconSize * 0.15f

        // Линия стрелки
        drawLine(
            color = color,
            start = Offset(arrowStartX, arrowY),
            end = Offset(arrowStartX + arrowLength, arrowY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )

        // Голова стрелки (треугольник)
        val arrowPath = Path().apply {
            moveTo(arrowStartX + arrowLength, arrowY)
            lineTo(arrowStartX + arrowLength - arrowHeadSize, arrowY - arrowHeadSize / 2)
            lineTo(arrowStartX + arrowLength - arrowHeadSize, arrowY + arrowHeadSize / 2)
            close()
        }
        drawPath(
            path = arrowPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}
