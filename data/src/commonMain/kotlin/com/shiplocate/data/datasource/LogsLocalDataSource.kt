package com.shiplocate.data.datasource

import com.shiplocate.domain.model.logs.LogFile

/**
 * Интерфейс локального источника данных для работы с логами
 */
interface LogsLocalDataSource {
    /**
     * Получает список всех лог-файлов
     */
    suspend fun getLogFiles(): List<LogFile>

    /**
     * Удаляет лог-файл
     */
    suspend fun deleteLogFile(fileName: String): Boolean
}
