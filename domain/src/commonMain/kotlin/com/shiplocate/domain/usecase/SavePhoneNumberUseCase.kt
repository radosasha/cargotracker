package com.shiplocate.domain.usecase

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.repository.AuthRepository

/**
 * Use Case для сохранения номера телефона пользователя
 */
class SavePhoneNumberUseCase(
    private val authRepository: AuthRepository,
    private val logger: Logger,
) {

    /**
     * Сохраняет номер телефона пользователя
     */
    suspend operator fun invoke(phoneNumber: String) {
        try {
            logger.info(LogCategory.AUTH, "SavePhoneNumberUseCase: Saving phone number: $phoneNumber")
            authRepository.savePhoneNumber(phoneNumber)
            logger.info(LogCategory.AUTH, "SavePhoneNumberUseCase: Phone number saved successfully")
        } catch (e: Exception) {
            logger.error(LogCategory.AUTH, "SavePhoneNumberUseCase: Error saving phone number: ${e.message}", e)
        }
    }
}
