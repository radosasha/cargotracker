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

    /**
     * Создает ZIP архив из выбранных лог-файлов
     * @param selectedFiles список выбранных файлов для архивирования
     * @return путь к созданному архиву
     */
    suspend fun createArchive(selectedFiles: List<LogFile>): String

    /**
     * Удаляет архив после отправки
     * @param archivePath путь к архиву для удаления
     */
    suspend fun deleteArchive(archivePath: String): Boolean
}
