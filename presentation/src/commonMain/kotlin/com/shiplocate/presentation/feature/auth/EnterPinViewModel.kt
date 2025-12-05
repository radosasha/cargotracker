package com.shiplocate.presentation.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.auth.AuthSession
import com.shiplocate.domain.model.auth.SmsVerificationError
import com.shiplocate.domain.usecase.GetDeviceInfoUseCase
import com.shiplocate.domain.usecase.SavePhoneNumberUseCase
import com.shiplocate.domain.usecase.SendCachedTokenOnAuthUseCase
import com.shiplocate.domain.usecase.auth.SaveAuthSessionUseCase
import com.shiplocate.domain.usecase.auth.VerifySmsCodeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for EnterPinScreen
 */
class EnterPinViewModel(
    private val verifySmsCodeUseCase: VerifySmsCodeUseCase,
    private val saveAuthSessionUseCase: SaveAuthSessionUseCase,
    private val savePhoneNumberUseCase: SavePhoneNumberUseCase,
    private val sendCachedTokenOnAuthUseCase: SendCachedTokenOnAuthUseCase,
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase,
    private val logger: Logger,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EnterPinUiState())
    val uiState: StateFlow<EnterPinUiState> = _uiState.asStateFlow()

    init {
        logger.info(LogCategory.AUTH, "EnterPinViewModel: Initialized")
    }

    fun init(phone: String) {
        logger.info(LogCategory.AUTH, "EnterPinViewModel: Init with phone: $phone")
        _uiState.update { it.copy(phone = phone) }
    }

    fun onPinChanged(
        index: Int,
        digit: String,
    ) {
        val filtered = digit.filter { it.isDigit() }.take(1)

        val newPinDigits = _uiState.value.pinDigits.toMutableList()
        newPinDigits[index] = filtered

        logger.debug(LogCategory.AUTH, "EnterPinViewModel: PIN digit[$index] = '$filtered'")

        _uiState.update {
            it.copy(
                pinDigits = newPinDigits,
                errorMessage = null,
                remainingAttempts = null,
            )
        }

        // Auto-submit when all 6 digits are entered
        if (newPinDigits.all { it.isNotEmpty() }) {
            val pin = newPinDigits.joinToString("")
            logger.info(LogCategory.AUTH, "EnterPinViewModel: PIN complete: $pin - auto-submitting")
            verifyPin()
        }
    }

    fun onPinValueChanged(pin: String) {
        // Фильтруем только цифры и ограничиваем длину до 6 символов
        val filtered = pin.filter { it.isDigit() }.take(6)

        // Преобразуем строку в список символов
        val newPinDigits = filtered.map { it.toString() }.toMutableList()
        // Дополняем до 6 элементов пустыми строками
        while (newPinDigits.size < 6) {
            newPinDigits.add("")
        }

        logger.debug(LogCategory.AUTH, "EnterPinViewModel: PIN changed: '$filtered'")

        _uiState.update {
            it.copy(
                pinDigits = newPinDigits,
                errorMessage = null,
                remainingAttempts = null,
            )
        }

        // Auto-submit when all 6 digits are entered
        if (filtered.length == 6) {
            logger.info(LogCategory.AUTH, "EnterPinViewModel: PIN complete: $filtered - auto-submitting")
            verifyPin()
        }
    }

    private fun verifyPin() {
        val state = _uiState.value
        val pin = state.pinDigits.joinToString("")

        if (pin.length != 6) {
            logger.warn(LogCategory.AUTH, "EnterPinViewModel: PIN length invalid: ${pin.length}/6")
            return
        }

        logger.info(LogCategory.AUTH, "EnterPinViewModel: Verifying PIN: $pin for phone: ${state.phone}")

        _uiState.update {
            it.copy(
                isVerifying = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            val deviceInfo = getDeviceInfo()
            verifySmsCodeUseCase(
                phone = state.phone,
                code = pin,
                deviceInfo = deviceInfo,
            )
                .onSuccess { authToken ->
                    logger.info(LogCategory.AUTH, "EnterPinViewModel: Verification successful!")
                    logger.debug(LogCategory.AUTH, "EnterPinViewModel: Token: ${authToken.token.take(20)}...")
                    logger.info(LogCategory.AUTH, "EnterPinViewModel: User: ${authToken.user.name} (${authToken.user.phone})")

                    // Save session
                    val session = AuthSession(
                        token = authToken.token,
                        user = authToken.user,
                    )
                    saveAuthSessionUseCase(session)
                    logger.info(LogCategory.AUTH, "EnterPinViewModel: Session saved")

                    // Save phone number without "+" for logs
                    val cleanPhone = state.phone.replace("+", "")
                    savePhoneNumberUseCase(cleanPhone)
                    logger.info(LogCategory.AUTH, "EnterPinViewModel: Phone number saved: $cleanPhone")

                    // Отправляем кешированный Firebase токен на сервер
                    sendCachedTokenOnAuthUseCase()

                    _uiState.update {
                        it.copy(
                            isVerifying = false,
                            navigateToHome = true,
                        )
                    }
                }
                .onFailure { error ->
                    when (error) {
                        is SmsVerificationError.CodeInvalid -> {
                            // Уже обработано: показываем inline с remainingAttempts
                            val attempts = error.remainingAttempts ?: 0
                            logger.warn(LogCategory.AUTH, "EnterPinViewModel: Invalid code: $attempts attempts remaining")
                            _uiState.update {
                                it.copy(
                                    isVerifying = false,
                                    errorMessage = error.message,
                                    navigateBackWithError = if (attempts == 0) error.message else null,
                                    remainingAttempts = attempts,
                                    pinDigits = List(6) { "" }, // Clear PIN
                                )
                            }
                        }

                        is SmsVerificationError.TooManyAttempts -> {
                            // Уже обработано: возврат на EnterPhoneScreen с диалогом
                            logger.warn(LogCategory.AUTH, "EnterPinViewModel: Too many attempts - navigating back")
                            _uiState.update {
                                it.copy(
                                    isVerifying = false,
                                    navigateBackWithError = error.message,
                                )
                            }
                        }

                        is SmsVerificationError.CodeExpired,
                        is SmsVerificationError.CodeNotFound,
                        is SmsVerificationError.CodeAlreadyUsed,
                            -> {
                            // Критические ошибки - возврат на EnterPhoneScreen с диалогом
                            logger.error(LogCategory.AUTH, "EnterPinViewModel: Critical error: ${error.code} - navigating back")
                            _uiState.update {
                                it.copy(
                                    isVerifying = false,
                                    navigateBackWithError = error.message,
                                )
                            }
                        }

                        is SmsVerificationError.UserBlocked -> {
                            // Блокировка пользователя - показываем диалог
                            logger.error(LogCategory.AUTH, "EnterPinViewModel: User blocked")
                            _uiState.update {
                                it.copy(
                                    isVerifying = false,
                                    showErrorDialog = true,
                                    errorDialogTitle = "Account Blocked",
                                    errorDialogMessage = error.message,
                                    pinDigits = List(6) { "" },
                                )
                            }
                        }

                        is SmsVerificationError.VerificationServiceError -> {
                            // Ошибка сервиса верификации - показываем диалог
                            logger.error(LogCategory.AUTH, "EnterPinViewModel: Verification service error")
                            _uiState.update {
                                it.copy(
                                    isVerifying = false,
                                    showErrorDialog = true,
                                    errorDialogTitle = "Service Temporarily Unavailable",
                                    errorDialogMessage =
                                        "Our servers are currently undergoing maintenance. " +
                                            "Please try again in a few minutes.",
                                    pinDigits = List(6) { "" },
                                )
                            }
                        }

                        is SmsVerificationError.RateLimitExceeded -> {
                            // Rate limit - показываем диалог
                            logger.error(LogCategory.AUTH, "EnterPinViewModel: Rate limit exceeded")
                            _uiState.update {
                                it.copy(
                                    isVerifying = false,
                                    showErrorDialog = true,
                                    errorDialogTitle = "Too Many Requests",
                                    errorDialogMessage = error.message,
                                    pinDigits = List(6) { "" },
                                )
                            }
                        }

                        is SmsVerificationError -> {
                            // Другие ошибки верификации - показываем диалог
                            logger.error(LogCategory.AUTH, "EnterPinViewModel: Verification error: ${error.code} - ${error.message}")
                            _uiState.update {
                                it.copy(
                                    isVerifying = false,
                                    showErrorDialog = true,
                                    errorDialogTitle = "Error",
                                    errorDialogMessage = error.message,
                                    pinDigits = List(6) { "" },
                                )
                            }
                        }

                        else -> {
                            // Неизвестные ошибки - показываем диалог
                            logger.error(LogCategory.AUTH, "EnterPinViewModel: Unknown error: ${error.message}")
                            _uiState.update {
                                it.copy(
                                    isVerifying = false,
                                    showErrorDialog = true,
                                    errorDialogTitle = "Error",
                                    errorDialogMessage = error.message ?: "An unknown error occurred. Please try again.",
                                    pinDigits = List(6) { "" },
                                )
                            }
                        }
                    }
                }
        }
    }

    private suspend fun getDeviceInfo(): String {
        return getDeviceInfoUseCase()
    }

    fun onNavigatedToHome() {
        _uiState.update { it.copy(navigateToHome = false) }
    }

    fun onNavigatedBack() {
        _uiState.update { it.copy(navigateBackWithError = null) }
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
}

data class EnterPinUiState(
    val phone: String = "",
    val pinDigits: List<String> = List(6) { "" },
    val isVerifying: Boolean = false,
    val errorMessage: String? = null,
    val remainingAttempts: Int? = null,
    val navigateToHome: Boolean = false,
    val navigateBackWithError: String? = null,
    val showErrorDialog: Boolean = false,
    val errorDialogTitle: String? = null,
    val errorDialogMessage: String? = null,
) {
    val isPinComplete: Boolean
        get() = pinDigits.all { it.isNotEmpty() }

    val isPinEmpty: Boolean
        get() = pinDigits.all { it.isEmpty() }
}
