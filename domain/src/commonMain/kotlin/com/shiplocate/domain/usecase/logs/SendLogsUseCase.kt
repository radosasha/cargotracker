package com.shiplocate.domain.usecase.logs

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.logs.LogFile
import com.shiplocate.domain.repository.AuthPreferencesRepository
import com.shiplocate.domain.repository.DeviceRepository
import com.shiplocate.domain.repository.LogsRepository
import com.shiplocate.domain.repository.PrefsRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Use Case для отправки лог-файлов на сервер
 */
class SendLogsUseCase(
    private val logsRepository: LogsRepository,
    private val prefsRepository: PrefsRepository,
    private val deviceRepository: DeviceRepository,
    private val authPreferencesRepository: AuthPreferencesRepository,
    private val logger: Logger,
) {

    /**
     * Отправляет выбранные лог-файлы на сервер через архив
     */
    suspend operator fun invoke(files: List<LogFile>): Result<Unit> {
        return try {
            logger.info(LogCategory.GENERAL, "SendLogsUseCase: Starting to send ${files.size} log files as archive")

            if (files.isEmpty()) {
                logger.warn(LogCategory.GENERAL, "SendLogsUseCase: No files to send")
                return Result.failure(IllegalArgumentException("No files to send"))
            }

            val actualClientId = getLogsClientId()
            logger.debug(LogCategory.GENERAL, "SendLogsUseCase: Generated clientId: $actualClientId")

            val result = logsRepository.sendLogFilesAsArchive(files, actualClientId)

            if (result.isSuccess) {
                logger.info(LogCategory.GENERAL, "SendLogsUseCase: Successfully sent ${files.size} log files as archive")
            } else {
                logger.error(
                    LogCategory.GENERAL,
                    "SendLogsUseCase: Failed to send log files as archive: ${result.exceptionOrNull()?.message}"
                )
            }

            result
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "SendLogsUseCase: Error sending log files as archive: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun getLogsClientId(): String {
        return if (authPreferencesRepository.hasSession()) {
            val phoneNumber = prefsRepository.getPhoneNumber()
            if (phoneNumber == null) {
                getAuthClientId("NoPhone")
            } else {
                getAuthClientId(phoneNumber)
            }
        } else {
            getNotAuthClientId()
        }
    }

    private suspend fun getAuthClientId(phoneNumber: String): String {
        val deviceInfo = getDeviceInfo()
        return "phone_${phoneNumber}_${deviceInfo}"
    }

    private suspend fun getNotAuthClientId(): String {
        val timestamp = getCurrentTimestamp()
        val deviceInfo = getDeviceInfo()
        return "device_${timestamp}_${deviceInfo}"
    }

    private suspend fun getDeviceInfo(): String {
        val platform = deviceRepository.getPlatform().lowercase()
        val model = deviceRepository.getDeviceModel().lowercase().replace(" ", "-")
        val osVersion = deviceRepository.getOsVersion()

        return when (platform) {
            "android" -> {
                val apiLevel = deviceRepository.getApiLevel()
                val manufacturer = model
                "android_${manufacturer}_${model}_api_${apiLevel}"
            }

            "ios" -> {
                "ios_${model}_ios_${extractIosVersion(osVersion)}"
            }

            else -> {
                "${platform}_${model}_${osVersion}"
            }
        }
    }

    private fun extractIosVersion(osVersion: String): String {
        return try {
            val versionMatch = Regex("iOS (\\d+\\.\\d+)").find(osVersion)
            versionMatch?.groupValues?.get(1) ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun getCurrentTimestamp(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return "${now.year}${now.monthNumber.toString().padStart(2, '0')}${now.dayOfMonth.toString().padStart(2, '0')}_${
            now.hour.toString().padStart(2, '0')
        }${now.minute.toString().padStart(2, '0')}${now.second.toString().padStart(2, '0')}"
    }
}
