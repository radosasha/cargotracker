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
     * Отправляет выбранные лог-файлы на сервер
     */
    suspend fun sendLogFiles(files: List<LogFile>): Result<Unit>

    /**
     * Удаляет лог-файл
     */
    suspend fun deleteLogFile(fileName: String): Boolean
}
