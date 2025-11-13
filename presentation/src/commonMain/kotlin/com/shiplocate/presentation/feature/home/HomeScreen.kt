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

/**
 * Главный экран приложения
 */
@Suppress("FunctionName")
@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    loadId: Long,
    viewModel: HomeViewModel,
    onNavigateToLogs: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(loadId) {
        viewModel.initialize(loadId)
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

        // Кнопка Start / Load delivered
        val isTrackingActive = uiState.trackingStatus == com.shiplocate.domain.model.TrackingStatus.ACTIVE
        val hasPermissions = uiState.permissionStatus?.hasAllPermissions ?: false
        
        Button(
            onClick = {
                if (!hasPermissions) {
                    viewModel.requestPermissions()
                } else if (!isTrackingActive) {
                    viewModel.startTracking()
                } else {
                    // Показываем диалог подтверждения для "Load delivered"
                    viewModel.showLoadDeliveredDialog()
                }
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

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка для перехода к логам
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
}
