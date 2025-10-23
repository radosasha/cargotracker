package com.shiplocate.domain.usecase.logs

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.logs.LogFile
import com.shiplocate.domain.repository.LogsRepository

/**
 * Use Case для получения списка лог-файлов
 */
class GetLogsUseCase(
    private val logsRepository: LogsRepository,
    private val logger: Logger,
) {
    /**
     * Получает список всех лог-файлов
     */
    suspend operator fun invoke(): List<LogFile> {
        return try {
            logger.debug(LogCategory.GENERAL, "GetLogsUseCase: Getting log files")
            val files = logsRepository.getLogFiles()
            logger.info(LogCategory.GENERAL, "GetLogsUseCase: Retrieved ${files.size} log files")
            files
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "GetLogsUseCase: Error getting log files: ${e.message}", e)
            emptyList()
        }
    }
}
