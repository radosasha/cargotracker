package com.shiplocate.presentation.feature.permissions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.min

/**
 * Экран для запроса разрешений приложения
 * Отображает три шага для получения необходимых разрешений
 */
@Suppress("FunctionName")
@Composable
fun PermissionsScreen(
    paddingValues: PaddingValues,
    viewModel: PermissionsViewModel,
    onContinue: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Автоматически переходим дальше, если все разрешения получены
    LaunchedEffect(uiState.hasAllPermissions) {
        if (uiState.hasAllPermissions) {
            onContinue()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        // Описание
        Text(
            text = "To help ShipLocate driver application track your routes accurately and keep everything running smoothly, please allow the following:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth(),
        )

        // Step 1: Precise Location
        PermissionStepCard(
            stepNumber = 1,
            title = "Turn on Precise Location for ShipLocate driver application.",
            isGranted = uiState.hasLocationPermission,
            onGrantClick = { viewModel.requestLocationPermission() },
        )

        // Step 2: Background Location
        PermissionStepCard(
            stepNumber = 2,
            title = "Allow Background Location so tracking continues even when the app isn't open.",
            isGranted = uiState.hasBackgroundLocationPermission,
            onGrantClick = { viewModel.requestBackgroundLocationPermission() },
        )

        // Step 3: GPS enabled
        PermissionStepCard(
            stepNumber = 3,
            title = "Keep Location Services (GPS) turned on to ensure ShipLocate records precise movement.",
            isGranted = uiState.isLocationEnabled,
            onGrantClick = { viewModel.requestEnableHighAccuracy() },
        )

        // Step 3: Battery Optimization
        PermissionStepCard(
            stepNumber = 4,
            title = "Enable Unrestricted Battery Use so the app stays active during your trips.",
            isGranted = uiState.isBatteryOptimizationDisabled,
            onGrantClick = { viewModel.requestBatteryOptimizationDisable() },
        )

        // Step 4: Notification Permission
        PermissionStepCard(
            stepNumber = 5,
            title = "Allow Notifications so you can receive important updates about your deliveries.",
            isGranted = uiState.hasNotificationPermission,
            onGrantClick = { viewModel.requestNotificationPermission() },
        )

        // Note
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
        ) {
            Text(
                text = "ShipLocate driver application only tracks your location while you're online. Remember to go offline once you've completed all your deliveries to stop tracking and help save battery.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }

        // Continue button
        Button(
            onClick = onContinue,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            enabled = uiState.hasAllPermissions,
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun PermissionStepCard(
    stepNumber: Int,
    title: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Step $stepNumber",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            if (isGranted) {
                // Зеленая круглая иконка с белой галочкой
                GrantedPermissionIcon(
                    modifier = Modifier.size(32.dp),
                )
            } else {
                // Кнопка Grant
                OutlinedButton(
                    onClick = onGrantClick,
                ) {
                    Text("Grant")
                }
            }
        }
    }
}

@Composable
private fun GrantedPermissionIcon(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color(0xFF4CAF50), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val iconSize = min(size.width, size.height) * 0.5f

            // Белая галочка
            val checkPath = Path().apply {
                moveTo(centerX - iconSize * 0.3f, centerY)
                lineTo(centerX - iconSize * 0.1f, centerY + iconSize * 0.2f)
                lineTo(centerX + iconSize * 0.3f, centerY - iconSize * 0.2f)
            }
            drawPath(
                checkPath,
                color = Color.White,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

