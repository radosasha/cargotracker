package com.shiplocate.domain.usecase.logs

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.logs.LogFile
import com.shiplocate.domain.repository.LogsRepository

actual class SendLogsUseCase(
    private val logsRepository: LogsRepository,
    private val logger: Logger,
) {

    /**
     * Отправляет выбранные лог-файлы на сервер через архив
     */
    actual suspend operator fun invoke(clientId: String, files: List<LogFile>): Result<Unit> {
        return try {
            logger.info(LogCategory.GENERAL, "SendLogsUseCase: Starting to send ${files.size} log files as archive")

            if (files.isEmpty()) {
                logger.warn(LogCategory.GENERAL, "SendLogsUseCase: No files to send")
                return Result.failure(IllegalArgumentException("No files to send"))
            }

            logger.debug(LogCategory.GENERAL, "SendLogsUseCase: Generated clientId: $clientId")

            val result = logsRepository.sendLogFilesAsArchive(files, clientId)

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
}

