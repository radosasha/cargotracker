package com.tracker.presentation.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tracker.presentation.component.MessageCard

/**
 * Главный экран приложения - только кнопка Start
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок
        Text(
            text = "GPS Tracker",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Статус трекинга
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.trackingStatus == com.tracker.domain.model.TrackingStatus.ACTIVE) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (uiState.trackingStatus == com.tracker.domain.model.TrackingStatus.ACTIVE) "Tracking Active" else "Tracking Stopped",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (uiState.trackingStatus == com.tracker.domain.model.TrackingStatus.ACTIVE) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (uiState.trackingStatus == com.tracker.domain.model.TrackingStatus.ACTIVE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "GPS coordinates are being tracked",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Кнопка Start
        Button(
            onClick = {
                val hasPermissions = uiState.permissionStatus?.hasAllPermissions ?: false
                if (!hasPermissions) {
                    viewModel.requestPermissions()
                } else if (uiState.trackingStatus != com.tracker.domain.model.TrackingStatus.ACTIVE) {
                    viewModel.startTracking()
                } else {
                    viewModel.stopTracking()
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = when {
                    !(uiState.permissionStatus?.hasAllPermissions ?: false) -> "Start"
                    uiState.trackingStatus != com.tracker.domain.model.TrackingStatus.ACTIVE -> "Start"
                    else -> "Stop"
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Кнопка тестирования сервера
        OutlinedButton(
            onClick = { viewModel.onTestServer() },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test Server Connection")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Сообщения
        uiState.message?.let { message ->
            MessageCard(
                message = message,
                messageType = uiState.messageType,
                onDismiss = viewModel::clearMessage
            )
        }
        
        // Загрузка
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}