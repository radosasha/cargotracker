package com.shiplocate.domain.repository

import com.shiplocate.domain.model.logs.LogFile

/**
 * Интерфейс репозитория для работы с логами
 */
interface LogsRepository {
    /**
     * Получает список всех лог-файлов
     */
    suspend fun getLogFiles(): List<LogFile>

    /**
     * Отправляет выбранные лог-файлы на сервер через архив
     * @param files список выбранных файлов для отправки
     * @param clientId идентификатор клиента
     * @param authToken токен аутентификации
     */
    suspend fun sendLogFilesAsArchive(files: List<LogFile>, clientId: String): Result<Unit>

    suspend fun sendLogFiles(files: List<LogFile>, clientId: String): Result<Unit>

    /**
     * Удаляет лог-файл
     */
    suspend fun deleteLogFile(fileName: String): Boolean

}
