package com.shiplocate.domain.usecase.logs

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.logs.LogFile
import com.shiplocate.domain.repository.LogsRepository

/**
 * Use Case для отправки лог-файлов на сервер
 */
class SendLogsUseCase(
    private val logsRepository: LogsRepository,
    private val logger: Logger,
) {
    /**
     * Отправляет выбранные лог-файлы на сервер
     */
    suspend operator fun invoke(files: List<LogFile>): Result<Unit> {
        return try {
            logger.info(LogCategory.GENERAL, "SendLogsUseCase: Starting to send ${files.size} log files")

            if (files.isEmpty()) {
                logger.warn(LogCategory.GENERAL, "SendLogsUseCase: No files to send")
                return Result.failure(IllegalArgumentException("No files to send"))
            }

            val result = logsRepository.sendLogFiles(files)

            if (result.isSuccess) {
                logger.info(LogCategory.GENERAL, "SendLogsUseCase: Successfully sent ${files.size} log files")
            } else {
                logger.error(LogCategory.GENERAL, "SendLogsUseCase: Failed to send log files: ${result.exceptionOrNull()?.message}")
            }

            result
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "SendLogsUseCase: Error sending log files: ${e.message}", e)
            Result.failure(e)
        }
    }
}
