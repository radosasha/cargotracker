package com.shiplocate.data.datasource.impl

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.LogsRemoteDataSource
import com.shiplocate.data.network.api.LogsApi

class LogsRemoteDataSourceImpl(
    private val logsApi: LogsApi,
    private val logger: Logger,
) : LogsRemoteDataSource {

    override suspend fun sendLogArchive(archivePath: String, clientId: String): Result<Unit> {
        return try {
            logger.info(LogCategory.NETWORK, "LogsRemoteDataSource: Sending archive to server: $archivePath")
            val result = logsApi.sendLogArchive(archivePath, clientId)

            if (result.isSuccess) {
                logger.info(LogCategory.NETWORK, "LogsRemoteDataSource: Successfully sent archive")
                Result.success(Unit)
            } else {
                logger.error(LogCategory.NETWORK, "LogsRemoteDataSource: Failed to send archive: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            logger.error(LogCategory.NETWORK, "LogsRemoteDataSource: Error sending archive: ${e.message}", e)
            Result.failure(e)
        }
    }
}
