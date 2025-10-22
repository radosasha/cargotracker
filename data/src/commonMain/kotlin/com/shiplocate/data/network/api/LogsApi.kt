package com.shiplocate.data.network.api

import com.shiplocate.domain.model.logs.LogFile

/**
 * Интерфейс API для отправки логов на сервер
 */
interface LogsApi {
    /**
     * Отправляет лог-файлы на сервер
     */
    suspend fun sendLogFiles(files: List<LogFile>): Result<Unit>
}
