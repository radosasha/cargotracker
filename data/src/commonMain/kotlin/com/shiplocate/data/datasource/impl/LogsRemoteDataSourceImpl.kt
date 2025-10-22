package com.shiplocate.data.datasource.impl

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.LogsRemoteDataSource
import com.shiplocate.data.network.api.LogsApi
import com.shiplocate.domain.model.logs.LogFile

class LogsRemoteDataSourceImpl(
    private val logsApi: LogsApi,
    private val logger: Logger,
) : LogsRemoteDataSource {
    override suspend fun sendLogFiles(files: List<LogFile>): Result<Unit> {
        return try {
            logger.info(LogCategory.NETWORK, "LogsRemoteDataSource: Sending ${files.size} log files to server")
            val result = logsApi.sendLogFiles(files)

            if (result.isSuccess) {
                logger.info(LogCategory.NETWORK, "LogsRemoteDataSource: Successfully sent log files")
                Result.success(Unit)
            } else {
                logger.error(LogCategory.NETWORK, "LogsRemoteDataSource: Failed to send log files: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            logger.error(LogCategory.NETWORK, "LogsRemoteDataSource: Error sending log files: ${e.message}", e)
            Result.failure(e)
        }
    }
}
