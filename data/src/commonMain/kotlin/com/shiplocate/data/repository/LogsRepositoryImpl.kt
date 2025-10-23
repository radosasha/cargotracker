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

    override suspend fun sendLogFilesAsArchive(files: List<LogFile>, clientId: String): Result<Unit> {
        var archivePath: String? = null
        return try {
            logger.info(LogCategory.GENERAL, "LogsRepository: Sending ${files.size} log files as archive")
            
            // Создаем архив
            archivePath = logsLocalDataSource.createArchive(files)
            logger.debug(LogCategory.GENERAL, "LogsRepository: Created archive: $archivePath")
            
            // Отправляем архив
            val result = logsRemoteDataSource.sendLogArchive(archivePath, clientId)
            
            if (result.isSuccess) {
                logger.info(LogCategory.GENERAL, "LogsRepository: Successfully sent archive")
                // Удаляем отправленные файлы
                files.forEach { file ->
                    logsLocalDataSource.deleteLogFile(file.name)
                }
                logger.info(LogCategory.GENERAL, "LogsRepository: Deleted ${files.size} sent log files")
            } else {
                logger.error(LogCategory.GENERAL, "LogsRepository: Failed to send archive: ${result.exceptionOrNull()?.message}")
            }
            
            result
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "LogsRepository: Error sending archive: ${e.message}", e)
            Result.failure(e)
        } finally {
            // Удаляем архив в любом случае
            archivePath?.let { path ->
                try {
                    logsLocalDataSource.deleteArchive(path)
                    logger.debug(LogCategory.GENERAL, "LogsRepository: Cleaned up archive: $path")
                } catch (e: Exception) {
                    logger.warn(LogCategory.GENERAL, "LogsRepository: Failed to cleanup archive: ${e.message}")
                }
            }
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
