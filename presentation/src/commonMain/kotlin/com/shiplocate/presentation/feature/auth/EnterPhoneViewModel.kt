package com.shiplocate.presentation.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.auth.Country
import com.shiplocate.domain.model.auth.SmsRequestError
import com.shiplocate.domain.usecase.auth.RequestSmsCodeUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for EnterPhoneScreen
 */
class EnterPhoneViewModel(
    private val requestSmsCodeUseCase: RequestSmsCodeUseCase,
    private val logger: Logger,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EnterPhoneUiState())
    val uiState: StateFlow<EnterPhoneUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        logger.info(LogCategory.AUTH, "EnterPhoneViewModel: Initialized")
    }

    fun onCountrySelected(country: Country) {
        logger.info(LogCategory.AUTH, "EnterPhoneViewModel: Country selected: ${country.name} (${country.dialCode})")
        _uiState.update { it.copy(selectedCountry = country, phoneNumber = "") }
    }

    fun onPhoneNumberChanged(number: String) {
        // Only allow digits
        val filtered = number.filter { it.isDigit() }

        // Limit to country's phone length
        val maxLength = _uiState.value.selectedCountry.phoneLength
        val limited = filtered.take(maxLength)

        logger.debug(LogCategory.AUTH, "EnterPhoneViewModel: Phone changed: '$limited' (${limited.length}/$maxLength)")

        _uiState.update {
            it.copy(
                phoneNumber = limited,
                errorMessage = null,
            )
        }
    }

    fun onAgreementChanged(agreed: Boolean) {
        logger.debug(LogCategory.AUTH, "EnterPhoneViewModel: Agreement changed: $agreed")
        _uiState.update { it.copy(agreedToSms = agreed) }
    }

    fun onSendCodeClicked() {
        val state = _uiState.value

        logger.info(LogCategory.AUTH, "EnterPhoneViewModel: Send code clicked")

        // Validate phone number length
        val requiredLength = state.selectedCountry.phoneLength
        if (state.phoneNumber.length < requiredLength) {
            logger.warn(LogCategory.AUTH, "EnterPhoneViewModel: Validation failed: ${state.phoneNumber.length}/$requiredLength digits")
            _uiState.update {
                it.copy(errorMessage = "Phone number must be $requiredLength digits")
            }
            return
        }

        // Build full phone number
        val fullPhone = "${state.selectedCountry.dialCode}${state.phoneNumber}"

        logger.info(LogCategory.AUTH, "EnterPhoneViewModel: Requesting SMS for: $fullPhone")
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            requestSmsCodeUseCase(fullPhone)
                .onSuccess { response ->
                    logger.info(LogCategory.AUTH, "EnterPhoneViewModel: SMS sent successfully: ${response.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            navigateToPinScreen = true,
                            sentPhone = fullPhone,
                        )
                    }
                }
                .onFailure { error ->
                    when (error) {
                        is SmsRequestError.RateLimitExceeded -> {
                            // Уже обработано: показываем таймер
                            val retrySeconds = error.retryAfterSeconds ?: 60
                            logger.warn(LogCategory.AUTH, "EnterPhoneViewModel: Rate limited: retry after ${retrySeconds}s")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isRateLimited = true,
                                    rateLimitSeconds = retrySeconds,
                                    errorMessage = null, // Очищаем, т.к. таймер уже показывает состояние
                                )
                            }
                            startRateLimitTimer(retrySeconds)
                        }

                        is SmsRequestError.ValidationError -> {
                            // Показываем диалог с ошибкой валидации
                            logger.error(LogCategory.AUTH, "EnterPhoneViewModel: Validation error: ${error.message}")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    showErrorDialog = true,
                                    errorDialogTitle = "Invalid Phone Number",
                                    errorDialogMessage = error.message,
                                )
                            }
                        }

                        is SmsRequestError.SmsServiceError -> {
                            // Показываем диалог с ошибкой недоступности сервиса
                            logger.error(LogCategory.AUTH, "EnterPhoneViewModel: SMS service error: ${error.message}")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    showErrorDialog = true,
                                    errorDialogTitle = "Service Temporarily Unavailable",
                                    errorDialogMessage =
                                        "Our servers are currently undergoing maintenance. " +
                                            "Please try again in a few minutes.",
                                )
                            }
                        }

                        is SmsRequestError -> {
                            // Показываем диалог для других ошибок
                            logger.error(LogCategory.AUTH, "EnterPhoneViewModel: SMS request error: ${error.code} - ${error.message}")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    showErrorDialog = true,
                                    errorDialogTitle = "Error",
                                    errorDialogMessage = error.message,
                                )
                            }
                        }

                        else -> {
                            // Показываем диалог для неизвестных ошибок
                            logger.error(LogCategory.AUTH, "EnterPhoneViewModel: Unknown error: ${error.message}")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    showErrorDialog = true,
                                    errorDialogTitle = "Error",
                                    errorDialogMessage = error.message ?: "An unknown error occurred. Please try again.",
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun startRateLimitTimer(seconds: Long) {
        logger.info(LogCategory.AUTH, "EnterPhoneViewModel: Starting rate limit timer: ${seconds}s")
        timerJob?.cancel()
        timerJob =
            viewModelScope.launch {
                var remaining = seconds
                while (remaining > 0) {
                    _uiState.update { it.copy(rateLimitSeconds = remaining) }
                    delay(1000)
                    remaining--
                }
                logger.info(LogCategory.AUTH, "EnterPhoneViewModel: Rate limit timer finished")
                _uiState.update {
                    it.copy(
                        isRateLimited = false,
                        rateLimitSeconds = 0,
                        errorMessage = null,
                    )
                }
            }
    }

    fun onNavigatedToPinScreen() {
        _uiState.update { it.copy(navigateToPinScreen = false) }
    }

    fun showTooManyAttemptsDialog(message: String) {
        _uiState.update {
            it.copy(
                showTooManyAttemptsDialog = true,
                tooManyAttemptsMessage = message,
            )
        }
    }

    fun onDismissTooManyAttemptsDialog() {
        _uiState.update {
            it.copy(
                showTooManyAttemptsDialog = false,
                tooManyAttemptsMessage = null,
            )
        }
    }

    fun onDismissErrorDialog() {
        _uiState.update {
            it.copy(
                showErrorDialog = false,
                errorDialogTitle = null,
                errorDialogMessage = null,
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        logger.info(LogCategory.AUTH, "EnterPhoneViewModel: Cleared")
        timerJob?.cancel()
    }
}

data class EnterPhoneUiState(
    val selectedCountry: Country = Country.US,
    val phoneNumber: String = "",
    val agreedToSms: Boolean = false,
    val isLoading: Boolean = false,
    val isRateLimited: Boolean = false,
    val rateLimitSeconds: Long = 0,
    val errorMessage: String? = null,
    val navigateToPinScreen: Boolean = false,
    val sentPhone: String = "",
    val showTooManyAttemptsDialog: Boolean = false,
    val tooManyAttemptsMessage: String? = null,
    val showErrorDialog: Boolean = false,
    val errorDialogTitle: String? = null,
    val errorDialogMessage: String? = null,
) {
    val isPhoneValid: Boolean
        get() = phoneNumber.length == selectedCountry.phoneLength

    val canSendCode: Boolean
        get() = isPhoneValid && agreedToSms && !isLoading && !isRateLimited

    val remainingDigits: Int
        get() = selectedCountry.phoneLength - phoneNumber.length
}
