package com.tracker.presentation.feature.auth

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnterPinScreen(
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

            // PIN input fields - ЦЕНТР ЭКРАНА
            PinInputFields(
                pinDigits = uiState.pinDigits,
                onPinChanged = viewModel::onPinChanged,
                onPinDigitCleared = viewModel::onPinDigitCleared,
                enabled = !uiState.isVerifying,
            )

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

@Composable
private fun PinInputFields(
    pinDigits: List<String>,
    onPinChanged: (Int, String) -> Unit,
    onPinDigitCleared: (Int) -> Unit,
    enabled: Boolean,
) {
    val focusRequesters = remember { List(6) { FocusRequester() } }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp), // Уменьшено с 8dp до 4dp
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        pinDigits.forEachIndexed { index, digit ->
            PinDigitField(
                digit = digit,
                onDigitChanged = { newDigit ->
                    // Если текущее поле уже заполнено и вводится новая цифра
                    if (digit.isNotEmpty() && index < 5) {
                        // Добавляем цифру в следующее поле
                        onPinChanged(index + 1, newDigit)
                        focusRequesters[index + 1].requestFocus()
                    } else {
                        // Обычный ввод
                        onPinChanged(index, newDigit)
                        // Auto-focus next field
                        if (newDigit.isNotEmpty() && index < 5) {
                            focusRequesters[index + 1].requestFocus()
                        }
                    }
                },
                onDigitCleared = {
                    onPinDigitCleared(index)
                    // Переходим к предыдущему полю и ставим курсор в конец
                    if (index > 0) {
                        focusRequesters[index - 1].requestFocus()
                    }
                },
                focusRequester = focusRequesters[index],
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
        }
    }

    // Auto-focus first field on mount
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }
}

@Composable
private fun PinDigitField(
    digit: String,
    onDigitChanged: (String) -> Unit,
    onDigitCleared: () -> Unit,
    focusRequester: FocusRequester,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var localValue by remember(digit) { mutableStateOf(digit) }

    TextField(
        value = localValue,
        onValueChange = { newValue ->
            when {
                newValue.isEmpty() && localValue.isNotEmpty() -> {
                    // Backspace pressed - очищаем поле и переходим к предыдущему
                    localValue = ""
                    onDigitCleared()
                }
                newValue.length == 1 && newValue.all { it.isDigit() } -> {
                    // Valid digit entered
                    localValue = newValue
                    onDigitChanged(newValue)
                }
                newValue.length > 1 -> {
                    // Если поле уже заполнено и вводится новая цифра
                    if (localValue.isNotEmpty()) {
                        // Берем новую цифру (последнюю введенную)
                        val newDigit = newValue.last().toString()
                        if (newDigit.all { it.isDigit() }) {
                            // Передаем новую цифру (будет добавлена в следующее поле)
                            onDigitChanged(newDigit)
                        }
                    } else {
                        // Paste or multiple chars - take first digit
                        val firstDigit = newValue.firstOrNull()?.toString() ?: ""
                        if (firstDigit.all { it.isDigit() }) {
                            localValue = firstDigit
                            onDigitChanged(firstDigit)
                        }
                    }
                }
            }
        },
        modifier =
            modifier
                .aspectRatio(1f / 1.5f) // Высота в 1.5 раза больше ширины
                .focusRequester(focusRequester)
                .border(
                    width = 2.dp,
                    color =
                        if (digit.isNotEmpty()) {
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
        textStyle =
            MaterialTheme.typography.headlineLarge.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
    )
}
