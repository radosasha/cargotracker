package com.shiplocate.data.datasource.impl

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.core.logging.appender.FileAppender
import com.shiplocate.core.logging.files.FilesManager
import com.shiplocate.core.logging.files.FileInfo
import com.shiplocate.data.datasource.LogsLocalDataSource
import com.shiplocate.domain.model.logs.LogFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Реализация локального источника данных для работы с логами
 */
class LogsLocalDataSourceImpl(
    private val fileAppender: FileAppender,
    private val filesManager: FilesManager,
    private val logger: Logger,
) : LogsLocalDataSource {

    override suspend fun getLogFiles(): List<LogFile> {
        return try {
            logger.debug(LogCategory.GENERAL, "LogsLocalDataSource: Getting list of log files")
            val fileNames = fileAppender.getLogFiles()
            val logFiles = fileNames.map { fileName ->
                val size = fileAppender.getLogFileSize(fileName)
                LogFile(name = fileName, size = size)
            }
            logger.info(LogCategory.GENERAL, "LogsLocalDataSource: Found ${logFiles.size} log files")
            logFiles
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "LogsLocalDataSource: Error getting log files: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun deleteLogFile(fileName: String): Boolean {
        return try {
            logger.debug(LogCategory.GENERAL, "LogsLocalDataSource: Deleting log file: $fileName")
            val result = fileAppender.deleteLogFile(fileName)
            if (result) {
                logger.info(LogCategory.GENERAL, "LogsLocalDataSource: Successfully deleted log file: $fileName")
            } else {
                logger.warn(LogCategory.GENERAL, "LogsLocalDataSource: Failed to delete log file: $fileName")
            }
            result
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "LogsLocalDataSource: Error deleting log file $fileName: ${e.message}", e)
            false
        }
    }

    override suspend fun createArchive(selectedFiles: List<LogFile>): String {
        return withContext(Dispatchers.Default) {
            try {
                logger.debug(LogCategory.GENERAL, "LogsLocalDataSource: Creating archive for ${selectedFiles.size} files")
                
                // Создаем уникальное имя архива с временной меткой
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val timestamp = "${now.year}${now.monthNumber.toString().padStart(2, '0')}${now.dayOfMonth.toString().padStart(2, '0')}_${now.hour.toString().padStart(2, '0')}${now.minute.toString().padStart(2, '0')}${now.second.toString().padStart(2, '0')}"
                val archiveName = "logs_$timestamp.zip"
                
                // Получаем путь к директории логов
                val logDirectoryPath = fileAppender.getLogDirectoryPath()
                val archivePath = "$logDirectoryPath/$archiveName"
                
                // Подготавливаем файлы для архивирования
                val filesToArchive = selectedFiles.mapNotNull { logFile ->
                    try {
                        val fileContent = fileAppender.getLogFileSource(logFile.name)
                        FileInfo(logFile.name, fileContent)
                    } catch (e: Exception) {
                        logger.warn(LogCategory.GENERAL, "LogsLocalDataSource: Failed to read ${logFile.name}: ${e.message}")
                        null
                    }
                }

                // Создаем ZIP архив через FilesManager
                val resultPath = filesManager.createZipArchive(filesToArchive, archivePath)
                
                logger.info(LogCategory.GENERAL, "LogsLocalDataSource: Successfully created archive: $archiveName")
                resultPath
                
            } catch (e: Exception) {
                logger.error(LogCategory.GENERAL, "LogsLocalDataSource: Error creating archive: ${e.message}", e)
                throw e
            }
        }
    }

    override suspend fun deleteArchive(archivePath: String): Boolean {
        return withContext(Dispatchers.Default) {
            try {
                logger.debug(LogCategory.GENERAL, "LogsLocalDataSource: Deleting archive: $archivePath")
                
                val result = filesManager.deleteFile(archivePath)
                
                if (result) {
                    logger.info(LogCategory.GENERAL, "LogsLocalDataSource: Successfully deleted archive: $archivePath")
                } else {
                    logger.warn(LogCategory.GENERAL, "LogsLocalDataSource: Archive not found or already deleted: $archivePath")
                }
                result
            } catch (e: Exception) {
                logger.error(LogCategory.GENERAL, "LogsLocalDataSource: Error deleting archive $archivePath: ${e.message}", e)
                false
            }
        }
    }
}
