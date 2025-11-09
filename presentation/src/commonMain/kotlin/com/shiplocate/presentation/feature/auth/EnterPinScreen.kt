package com.shiplocate.presentation.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Suppress("FunctionName")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnterPinScreen(
    paddingValues: PaddingValues,
    phone: String,
    onNavigateToHome: () -> Unit,
    onNavigateBack: (String?) -> Unit,
    viewModel: EnterPinViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Initialize with phone
    LaunchedEffect(phone) {
        viewModel.init(phone)
    }

    // Handle navigation to home
    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) {
            onNavigateToHome()
            viewModel.onNavigatedToHome()
        }
    }

    // Handle navigation back with error
    LaunchedEffect(uiState.navigateBackWithError) {
        if (uiState.navigateBackWithError != null) {
            onNavigateBack(uiState.navigateBackWithError)
            viewModel.onNavigatedBack()
        }
    }

    // Error dialog
    if (uiState.showErrorDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissErrorDialog() },
            title = { Text(uiState.errorDialogTitle ?: "Error") },
            text = { Text(uiState.errorDialogMessage ?: "An error occurred") },
            confirmButton = {
                TextButton(onClick = { viewModel.onDismissErrorDialog() }) {
                    Text("OK")
                }
            },
        )
    }

    // Detect keyboard visibility (simplified approach using WindowInsets)
    val imeVisible = if (WindowInsets.ime.getBottom(LocalDensity.current) > 0) true else false

    // Animate vertical offset when keyboard appears
    val verticalOffset by animateDpAsState(
        targetValue = if (imeVisible) (-80).dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "verticalOffset",
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(), // Adjust for keyboard
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .offset(y = verticalOffset),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Top content (above PIN fields)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 32.dp),
            ) {
                Text(
                    text = "Enter Verification Code",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "Code sent to $phone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            // PIN input field - одно поле ввода
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                PinInputField(
                    pin = uiState.pinDigits.joinToString(""),
                    onPinChanged = viewModel::onPinValueChanged,
                    enabled = !uiState.isVerifying,
                )
            }

            // Bottom content (below PIN fields)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 32.dp),
            ) {
                // Verifying indicator
                AnimatedVisibility(
                    visible = uiState.isVerifying,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = "Verifying code...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Error message
                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }

                // Remaining attempts
                AnimatedVisibility(
                    visible = uiState.remainingAttempts != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    val attempts = uiState.remainingAttempts ?: 0
                    Text(
                        text =
                            if (attempts > 0) {
                                "$attempts attempt${if (attempts != 1) "s" else ""} remaining"
                            } else {
                                "No attempts remaining"
                            },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun PinInputField(
    pin: String,
    onPinChanged: (String) -> Unit,
    enabled: Boolean,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var previousLength by remember { mutableStateOf(pin.length) }
    
    // Автоматически скрываем клавиатуру, когда введен последний символ (6-й)
    // Скрываем только если длина увеличилась (добавлен символ), а не уменьшилась (удален символ)
    LaunchedEffect(pin) {
        val currentLength = pin.length
        val maxLength = 6
        
        // Скрываем только если:
        // 1. Длина достигла максимума (6 символов)
        // 2. Длина увеличилась (добавлен символ, а не удален)
        if (currentLength == maxLength && currentLength > previousLength) {
            keyboardController?.hide()
        }
        
        previousLength = currentLength
    }

    TextField(
        value = pin,
        onValueChange = { newValue ->
            // Фильтруем только цифры и ограничиваем длину до 6 символов
            val filtered = newValue.filter { it.isDigit() }.take(6)
            onPinChanged(filtered)
        },
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .border(
                width = 2.dp,
                color = if (pin.length == 6) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                shape = RoundedCornerShape(12.dp),
            )
            .background(
                color = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
            ),
        enabled = enabled,
        placeholder = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "000000",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.sp, // Убираем letterSpacing, чтобы все 6 цифр поместились
                        fontSize = 28.sp, // Уменьшаем размер шрифта
                    ),
                )
            }
        },
        textStyle = MaterialTheme.typography.headlineMedium.copy(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace, // Моноширинный шрифт для равномерного распределения цифр
            letterSpacing = 0.sp, // Убираем letterSpacing, чтобы все 6 цифр поместились
            fontSize = 28.sp, // Уменьшаем размер шрифта
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
    )
}
