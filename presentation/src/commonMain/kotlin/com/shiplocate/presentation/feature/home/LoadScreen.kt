package com.shiplocate.presentation.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shiplocate.presentation.component.MessageCard
import com.shiplocate.presentation.component.StopsTimeline
import com.shiplocate.presentation.model.LoadStatus

/**
 * Главный экран приложения
 */
@Suppress("FunctionName")
@Composable
fun LoadScreen(
    paddingValues: PaddingValues,
    loadId: Long,
    viewModel: LoadViewModel,
    onNavigateToLogs: () -> Unit = {},
    onNavigateBack: (wasRejected: Boolean, switchToActive: Boolean) -> Unit = { _, _ -> },
    onNavigateToPermissions: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(loadId) {
        viewModel.initialize(loadId)
    }

    // Обработка навигации назад после успешного "Load delivered"
    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            onNavigateBack(false, false)
            // Сбрасываем флаг после навигации
            viewModel.resetNavigateBackFlag()
        }
    }

    // Обработка навигации назад после успешного "Reject load"
    LaunchedEffect(uiState.shouldNavigateBackAfterReject) {
        if (uiState.shouldNavigateBackAfterReject) {
            onNavigateBack(true, false) // Передаем true, чтобы показать диалог
            // Сбрасываем флаг после навигации
            viewModel.resetNavigateBackAfterRejectFlag()
        }
    }

    // Обработка навигации назад после успешного "Start tracking"
    LaunchedEffect(uiState.shouldNavigateBackAfterStart) {
        if (uiState.shouldNavigateBackAfterStart) {
            onNavigateBack(false, true) // Переключаемся на вкладку Active
            // Сбрасываем флаг после навигации
            viewModel.resetNavigateBackAfterStartFlag()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        // Заголовок Load вверху
        uiState.load?.let { load ->
            Text(
                text = "Load number: ${load.loadName}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stops Timeline
            if (load.stops.isNotEmpty()) {
                StopsTimeline(stops = load.stops)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Кнопки в зависимости от статуса Load
        val loadStatus = uiState.load?.loadStatus
        val isTrackingActive = uiState.trackingStatus == com.shiplocate.domain.model.TrackingStatus.ACTIVE
        val hasPermissions = uiState.permissionStatus?.hasAllPermissionsForTracking ?: false

        when (loadStatus) {
            LoadStatus.LOAD_STATUS_NOT_CONNECTED -> {
                // Показываем кнопку Start
                Button(
                    onClick = {
                        if (!hasPermissions) {
                            // Переходим на экран разрешений
                            onNavigateToPermissions()
                        } else {
                            viewModel.startTracking()
                        }
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Start")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка Reject
                Button(
                    onClick = { viewModel.showRejectLoadDialog() },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text("Reject")
                }
            }

            LoadStatus.LOAD_STATUS_CONNECTED -> {
                // Показываем кнопки "Load delivered" и "Reject load"
                Button(
                    onClick = {
                        // Показываем диалог подтверждения для "Load delivered"
                        viewModel.showLoadDeliveredDialog()
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (isTrackingActive) {
                        // Красный цвет для кнопки "Load delivered"
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                ) {
                    Text(
                        text =
                            when {
                                !hasPermissions -> "Start"
                                !isTrackingActive -> "Start"
                                else -> "Load delivered"
                            },
                    )
                }
            }

            LoadStatus.LOAD_STATUS_DISCONNECTED,
            LoadStatus.LOAD_STATUS_REJECTED,
            LoadStatus.LOAD_STATUS_UNKNOWN,
            null,
                -> {
                // Не показываем кнопок для других статусов
            }
        }

        // Кнопка для перехода к логам (всегда видна)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onNavigateToLogs,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("View Logs")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Сообщения
        uiState.message?.let { message ->
            MessageCard(
                message = message,
                messageType = uiState.messageType,
                onDismiss = viewModel::clearMessage,
            )
        }

        // Загрузка
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // Диалог подтверждения "Load delivered"
    if (uiState.showLoadDeliveredDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLoadDeliveredDialog() },
            title = {
                Text(
                    text = "Confirm Load Delivery",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to mark this load as delivered? This will stop tracking and disconnect from the load.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmLoadDelivered() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLoadDeliveredDialog() }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Диалог подтверждения "Reject load"
    if (uiState.showRejectLoadDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRejectLoadDialog() },
            title = {
                Text(
                    text = "Confirm Load Rejection",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to reject this load? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmRejectLoad() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRejectLoadDialog() }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Диалог о наличии активного груза
    if (uiState.showActiveLoadDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissActiveLoadDialog() },
            title = {
                Text(
                    text = "Active Load Exists",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = "Already there is an active load, close it to start a new one.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissActiveLoadDialog() }) {
                    Text("OK")
                }
            },
        )
    }
}
