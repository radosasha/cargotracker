package com.shiplocate.data.datasource.impl

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.core.logging.appender.FileAppender
import com.shiplocate.data.datasource.LogsLocalDataSource
import com.shiplocate.domain.model.logs.LogFile

/**
 * Реализация локального источника данных для работы с логами
 */
class LogsLocalDataSourceImpl(
    private val fileAppender: FileAppender,
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
}
