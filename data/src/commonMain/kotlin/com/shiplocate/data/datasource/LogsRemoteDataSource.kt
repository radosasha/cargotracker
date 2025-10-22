package com.shiplocate.data.datasource

import com.shiplocate.domain.model.logs.LogFile

/**
 * Интерфейс удаленного источника данных для работы с логами
 */
interface LogsRemoteDataSource {
    /**
     * Отправляет лог-файлы на сервер
     */
    suspend fun sendLogFiles(files: List<LogFile>): Result<Unit>
}
