package com.shiplocate.presentation.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.shiplocate.presentation.navigation.Screen

/**
 * Динамический TopAppBar для приложения
 * Обновляет заголовок и действия в зависимости от текущего экрана
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerTopAppBar(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier,
    onLongPressTitle: (() -> Unit)? = null,
) {
    // Определяем заголовок в зависимости от текущего маршрута
    val title = when {
        currentRoute?.startsWith(Screen.HOME) == true -> "GPS Tracker"
        currentRoute == Screen.LOADS -> "Loads"
        currentRoute == Screen.LOGS -> "Logs"
        currentRoute?.startsWith(Screen.ENTER_PIN) == true -> "Enter PIN"
        currentRoute == Screen.ENTER_PHONE -> "Enter Phone"
        else -> "ShipLocate"
    }

    // Показываем кнопку назад, если это не стартовый экран
    // В KMP Navigation previousBackStackEntry может работать нестабильно
    val canNavigateBack = when {
        currentRoute == Screen.ENTER_PHONE -> false
        currentRoute == Screen.LOADS -> false // Если это стартовый экран после авторизации
        else -> true
    }

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
