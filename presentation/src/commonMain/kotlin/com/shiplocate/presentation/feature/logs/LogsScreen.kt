package com.shiplocate.presentation.feature.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shiplocate.domain.model.logs.LogFile
import com.shiplocate.presentation.model.MessageType

/**
 * Экран для управления лог-файлами
 */
@Composable
fun LogsScreen(viewModel: LogsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Показываем сообщения через Snackbar
    LaunchedEffect(uiState.message, uiState.messageType) {
        uiState.message?.let { message ->
            val duration =
                when (uiState.messageType) {
                    MessageType.ERROR -> androidx.compose.material3.SnackbarDuration.Long
                    else -> androidx.compose.material3.SnackbarDuration.Short
                }
            snackbarHostState.showSnackbar(
                message = message,
                duration = duration,
            )
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
        ) {
            // Заголовок
            Text(
                text = "Logs",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // Кнопки управления выбором
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = viewModel::selectAllFiles,
                    enabled = !uiState.isSending && uiState.logFiles.isNotEmpty() && !uiState.isAllSelected,
                ) {
                    Text("Select all")
                }

                OutlinedButton(
                    onClick = viewModel::unselectAllFiles,
                    enabled = !uiState.isSending && uiState.hasSelection,
                ) {
                    Text("Unselect all")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Список файлов
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.logFiles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Logs not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.logFiles) { logFile ->
                        logFileItem(
                            logFile = logFile,
                            onToggleSelection = { viewModel.toggleFileSelection(logFile.name) },
                            enabled = !uiState.isSending,
                        )
                    }
                }
            }

            // Кнопка отправки внизу по центру
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = viewModel::sendSelectedFiles,
                enabled = uiState.canSend,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(16.dp).height(16.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Отправить (${uiState.selectedCount})")
            }
        }
    }
}

/**
 * Элемент списка лог-файла
 */
@Composable
private fun logFileItem(
    logFile: LogFile,
    onToggleSelection: () -> Unit,
    enabled: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = logFile.isSelected,
                onCheckedChange = { onToggleSelection() },
                enabled = enabled,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = logFile.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = logFile.formattedSize,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
