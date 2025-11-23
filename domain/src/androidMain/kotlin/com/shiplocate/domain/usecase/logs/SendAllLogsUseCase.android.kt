package com.shiplocate.domain.usecase.logs

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.repository.LogsRepository

actual class SendAllLogsUseCase(
    private val logsRepository: LogsRepository,
    private val logger: Logger,
) {

    /**
     * Отправляет все лог-файлы на сервер через архив
     * @param clientId идентификатор клиента
     */
    actual suspend operator fun invoke(clientId: String): Result<Unit> {
        return try {
            logger.info(LogCategory.GENERAL, "SendAllLogsUseCase: Starting to send all log files as archive")

            val result = logsRepository.sendAllLogFilesAsArchive(clientId)

            if (result.isSuccess) {
                logger.info(LogCategory.GENERAL, "SendAllLogsUseCase: Successfully sent all log files as archive")
            } else {
                logger.error(
                    LogCategory.GENERAL,
                    "SendAllLogsUseCase: Failed to send all log files as archive: ${result.exceptionOrNull()?.message}"
                )
            }

            result
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "SendAllLogsUseCase: Error sending all log files as archive: ${e.message}", e)
            Result.failure(e)
        }
    }
}

