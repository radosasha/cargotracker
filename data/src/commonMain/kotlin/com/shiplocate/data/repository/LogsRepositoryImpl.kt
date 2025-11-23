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

            // Используем переданный clientId
            logger.debug(LogCategory.GENERAL, "LogsRepository: Using clientId: $clientId")

            // Создаем архив
            archivePath = logsLocalDataSource.createArchive(logsLocalDataSource.getLogsDirectory(), files)
            logger.debug(LogCategory.GENERAL, "LogsRepository: Created archive: $archivePath")

            // Отправляем архив с переданным clientId
            val result = logsRemoteDataSource.sendLogArchive(archivePath, clientId)

            if (result.isSuccess) {
                logger.info(LogCategory.GENERAL, "LogsRepository: Successfully sent archive")
                // Оригинальные файлы НЕ удаляем - они остаются для дальнейшего использования
                logger.info(LogCategory.GENERAL, "LogsRepository: Archive sent successfully, original files preserved")
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

    override suspend fun sendLogFiles(files: List<LogFile>, clientId: String): Result<Unit> {
        return try {
            logger.info(LogCategory.GENERAL, "LogsRepository: Sending ${files.size} log files as archive")


            // Отправляем архив с переданным clientId
            logsLocalDataSource.getLogFiles()
            val result = logsRemoteDataSource.sendLog(files.map { logsLocalDataSource.getLogsDirectory() + "/" + it.name }, clientId)

            if (result.isSuccess) {
                logger.info(LogCategory.GENERAL, "LogsRepository: Successfully sent archive")
                // Оригинальные файлы НЕ удаляем - они остаются для дальнейшего использования
                logger.info(LogCategory.GENERAL, "LogsRepository: Archive sent successfully, original files preserved")
            } else {
                logger.error(LogCategory.GENERAL, "LogsRepository: Failed to send archive: ${result.exceptionOrNull()?.message}")
            }

            result
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "LogsRepository: Error sending archive: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun sendAllLogFilesAsArchive(clientId: String): Result<Unit> {
        return try {
            logger.info(LogCategory.GENERAL, "LogsRepository: Getting all log files to send as archive")

            // Получаем все лог-файлы
            val allFiles = getLogFiles()

            if (allFiles.isEmpty()) {
                logger.warn(LogCategory.GENERAL, "LogsRepository: No log files found to send")
                return Result.failure(IllegalStateException("No log files found"))
            }

            logger.info(LogCategory.GENERAL, "LogsRepository: Found ${allFiles.size} log files, sending as archive")

            // Отправляем все файлы через sendLogFilesAsArchive
            sendLogFilesAsArchive(allFiles, clientId)
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "LogsRepository: Error sending all log files as archive: ${e.message}", e)
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
