package com.shiplocate.data.repository

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.LogsLocalDataSource
import com.shiplocate.data.datasource.LogsRemoteDataSource
import com.shiplocate.domain.model.logs.LogFile
import com.shiplocate.domain.repository.LogsRepository

/**
 * Реализация репозитория для работы с логами
 */
class LogsRepositoryImpl(
    private val logsLocalDataSource: LogsLocalDataSource,
    private val logsRemoteDataSource: LogsRemoteDataSource,
    private val logger: Logger,
) : LogsRepository {

    override suspend fun getLogFiles(): List<LogFile> {
        return try {
            logger.debug(LogCategory.GENERAL, "LogsRepository: Getting log files")
            val files = logsLocalDataSource.getLogFiles()
            logger.info(LogCategory.GENERAL, "LogsRepository: Retrieved ${files.size} log files")
            files
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "LogsRepository: Error getting log files: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun sendLogFiles(files: List<LogFile>): Result<Unit> {
        return try {
            logger.info(LogCategory.GENERAL, "LogsRepository: Sending ${files.size} log files")
            val result = logsRemoteDataSource.sendLogFiles(files)
            
            if (result.isSuccess) {
                logger.info(LogCategory.GENERAL, "LogsRepository: Successfully sent log files")
                // Удаляем отправленные файлы
                files.forEach { file ->
                    logsLocalDataSource.deleteLogFile(file.name)
                }
                logger.info(LogCategory.GENERAL, "LogsRepository: Deleted ${files.size} sent log files")
            } else {
                logger.error(LogCategory.GENERAL, "LogsRepository: Failed to send log files: ${result.exceptionOrNull()?.message}")
            }
            
            result
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "LogsRepository: Error sending log files: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteLogFile(fileName: String): Boolean {
        return try {
            logger.debug(LogCategory.GENERAL, "LogsRepository: Deleting log file: $fileName")
            val result = logsLocalDataSource.deleteLogFile(fileName)
            if (result) {
                logger.info(LogCategory.GENERAL, "LogsRepository: Successfully deleted log file: $fileName")
            } else {
                logger.warn(LogCategory.GENERAL, "LogsRepository: Failed to delete log file: $fileName")
            }
            result
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "LogsRepository: Error deleting log file $fileName: ${e.message}", e)
            false
        }
    }
}
