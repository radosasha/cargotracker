package com.shiplocate.presentation.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shiplocate.domain.model.auth.Country
import com.shiplocate.presentation.component.ShipLocateLogo
import com.shiplocate.presentation.util.rememberEmailLauncher

@Suppress("FunctionName")
@Composable
fun EnterPhoneScreen(
    paddingValues: PaddingValues,
    onNavigateToPin: (String) -> Unit,
    onNavigateToLogs: () -> Unit = {},
    viewModel: EnterPhoneViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle navigation
    LaunchedEffect(uiState.navigateToPinScreen) {
        if (uiState.navigateToPinScreen) {
            onNavigateToPin(uiState.sentPhone)
            viewModel.onNavigatedToPinScreen()
        }
    }

    // Too many attempts dialog
    if (uiState.showTooManyAttemptsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissTooManyAttemptsDialog() },
            title = { Text("Too Many Attempts") },
            text = { Text(uiState.tooManyAttemptsMessage ?: "Please try again later") },
            confirmButton = {
                TextButton(onClick = { viewModel.onDismissTooManyAttemptsDialog() }) {
                    Text("OK")
                }
            },
        )
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

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            onNavigateToLogs()
                        },
                    )
                },
    ) {
        val screenHeight = maxHeight
        val logoHeight = screenHeight * 0.2f // 2% экрана

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Иконка - 25% экрана
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(logoHeight),
                contentAlignment = Alignment.Center,
            ) {
                ShipLocateLogo(
                    iconColor = Color.Black,
                    textColor = Color.Black,
                )
            }

            // Центральный контент - всё остальное пространство, выровнено по центру
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Title и подзаголовок плотно друг к другу
                    AutoSizeText(
                        text = "Sign in with your phone",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = "Enter your mobile number to receive a one-time SMS verification code.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Column(modifier = Modifier.padding(top = 15.dp)) {

                        // Phone input with country picker
                        PhoneInputField(
                            selectedCountry = uiState.selectedCountry,
                            phoneNumber = uiState.phoneNumber,
                            onCountrySelected = viewModel::onCountrySelected,
                            onPhoneNumberChanged = viewModel::onPhoneNumberChanged,
                            enabled = !uiState.isLoading && !uiState.isRateLimited,
                        )
                    }

                    // SMS agreement checkbox
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Checkbox(
                            checked = uiState.agreedToSms,
                            onCheckedChange = viewModel::onAgreementChanged,
                            enabled = !uiState.isLoading && !uiState.isRateLimited,
                        )
                        Text(
                            text = "I agree to receive one-time SMS verification codes for login.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // SMS disclaimer text
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 8.dp),
                    ) {
                        Text(
                            text = "Message and data rates may apply. Message frequency may vary.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Text STOP to opt out. Text HELP for support.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "We will not share your private information with third parties for marketing purposes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Validation hint
                    if (uiState.phoneNumber.isNotEmpty() && !uiState.isPhoneValid) {
                        Text(
                            text = "Enter ${uiState.remainingDigits} more digit${if (uiState.remainingDigits != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    // Error message
                    if (uiState.errorMessage != null && !uiState.isRateLimited) {
                        Text(
                            text = uiState.errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                    }

                    // Send code button
                    Button(
                        onClick = viewModel::onSendCodeClicked,
                        enabled = uiState.canSendCode,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text(
                                text = "Get Code",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }

                    // Rate limit timer
                    if (uiState.isRateLimited) {
                        RateLimitTimer(seconds = uiState.rateLimitSeconds)
                    }
                }
            }

            // Support text внизу - занимает минимально требуемое пространство
            SupportText()
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun PhoneInputField(
    selectedCountry: Country,
    phoneNumber: String,
    onCountrySelected: (Country) -> Unit,
    onPhoneNumberChanged: (String) -> Unit,
    enabled: Boolean,
) {
    var showCountryPicker by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    var previousLength by remember { mutableStateOf(phoneNumber.length) }

    // Автоматически скрываем клавиатуру, когда введен последний символ
    // Скрываем только если длина увеличилась (добавлен символ), а не уменьшилась (удален символ)
    LaunchedEffect(phoneNumber, selectedCountry.phoneLength) {
        val currentLength = phoneNumber.length
        val maxLength = selectedCountry.phoneLength

        // Скрываем только если:
        // 1. Длина достигла максимума
        // 2. Длина увеличилась (добавлен символ, а не удален)
        if (currentLength == maxLength && currentLength > previousLength) {
            keyboardController?.hide()
        }

        previousLength = currentLength
    }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp),
                )
                .background(
                    color = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Country picker button
        Row(
            modifier =
                Modifier
                    .clickable(enabled = enabled) { showCountryPicker = true }
                    .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = selectedCountry.flag,
                fontSize = 24.sp,
            )
            Text(
                text = selectedCountry.dialCode,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "▼",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Divider
        Box(
            modifier =
                Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(MaterialTheme.colorScheme.outline),
        )

        // Phone number input
        TextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChanged,
            modifier = Modifier.weight(1f),
            enabled = enabled,
            placeholder = {
                Text(
                    text = "0".repeat(selectedCountry.phoneLength),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            },
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

    // Country picker dropdown
    if (showCountryPicker) {
        CountryPickerDialog(
            selectedCountry = selectedCountry,
            onCountrySelected = {
                onCountrySelected(it)
                showCountryPicker = false
            },
            onDismiss = { showCountryPicker = false },
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun CountryPickerDialog(
    selectedCountry: Country,
    onCountrySelected: (Country) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Country") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Country.entries.forEach { country ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onCountrySelected(country) }
                                .background(
                                    color =
                                        if (country == selectedCountry) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            Color.Transparent
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                )
                                .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = country.flag,
                            fontSize = 24.sp,
                        )
                        Column {
                            Text(
                                text = country.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = country.dialCode,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Suppress("FunctionName")
@Composable
private fun InfoBox(
    icon: String,
    text: String,
    gradientColors: List<Color>,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(gradientColors),
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = icon,
                fontSize = 30.sp,
                color = Color(0xFFFFEB3B), // Yellow color for icons
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Текст, который автоматически уменьшает размер шрифта, чтобы поместиться в одну строку
 */
@Composable
private fun AutoSizeText(
    text: String,
    style: TextStyle,
    fontWeight: FontWeight,
    textAlign: TextAlign,
    modifier: Modifier = Modifier,
    minTextSize: TextUnit = 12.sp,
) {
    var textSize by remember(text, modifier) { mutableStateOf(style.fontSize) }

    // Сбрасываем размер при изменении текста или модификатора
    LaunchedEffect(text, modifier) {
        textSize = style.fontSize
    }

    // Используем onTextLayout для проверки, помещается ли текст
    Text(
        text = text,
        style = style.copy(
            fontSize = textSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
        ),
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Visible,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth && textSize > minTextSize) {
                // Текст не поместился, уменьшаем размер
                val newSize = textSize * 0.9f
                if (newSize >= minTextSize) {
                    textSize = newSize
                } else {
                    textSize = minTextSize
                }
            }
        },
    )
}

@Suppress("FunctionName")
@Composable
private fun SupportText() {
    val emailLauncher = rememberEmailLauncher()
    val email = "support@shiplocate.com"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Having troubles? ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = email,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            ),
            modifier = Modifier.clickable {
                emailLauncher.openEmail(email)
            },
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun RateLimitTimer(seconds: Long) {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    val timeString = "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${
        secs.toString().padStart(2, '0')
    }"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Too many requests",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "Please wait: $timeString",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
        )
    }
}
