package com.shiplocate.data.network.api

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.core.logging.files.FilesManager
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

/**
 * Реализация API для отправки логов на сервер
 */
class LogsApiImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val filesManager: FilesManager,
    private val logger: Logger,
) : LogsApi {

    override suspend fun sendLogArchive(archivePath: String, clientId: String): Result<Unit> {
        return try {
            logger.debug(LogCategory.NETWORK, "LogsApi: Starting to send archive: $archivePath")

            // Создаем ByteReadChannel для чтения архива
            val archSource = filesManager.getFileSource(archivePath)
            logger.debug(LogCategory.NETWORK, "LogsApi: Archive channel created for: $archivePath")


            val response = httpClient.submitFormWithBinaryData(
                url = "$baseUrl/api/logs/upload",
                formData = formData {
                    append("clientId", clientId)

                    append(
                        key = "archive",
                        value = InputProvider { archSource },
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, "application/zip")
                            append(HttpHeaders.ContentDisposition, "filename=\"logsfile${clientId}.zip\"")
                        }
                    )
                }
            )

            if (response.status.value in 200..299) {
                logger.info(LogCategory.NETWORK, "LogsApi: Successfully sent archive")
                Result.success(Unit)
            } else {
                logger.error(LogCategory.NETWORK, "LogsApi: Server error: ${response.status}")
                Result.failure(Exception("Server error: ${response.status}"))
            }

        } catch (e: Exception) {
            logger.error(LogCategory.NETWORK, "LogsApi: Error sending archive: ${e.message}", e)
            Result.failure(e)
        }
    }
}
