package com.shiplocate.core.logging.appender

import com.shiplocate.core.logging.LogEntry

/**
 * Appender для записи логов в файлы с поддержкой ротации
 */
expect class FileAppender : LogAppender {
    override suspend fun append(entry: LogEntry)

    /**
     * Получает список всех лог-файлов
     */
    suspend fun getLogFiles(): List<String>

    /**
     * Удаляет лог-файл
     */
    suspend fun deleteLogFile(fileName: String): Boolean

    /**
     * Получает размер лог-файла
     */
    suspend fun getLogFileSize(fileName: String): Long

    /**
     * Получает содержимое лог-файла
     */
    suspend fun getLogFileContent(fileName: String): ByteArray

    /**
     * Получает путь к директории с логами
     */
    suspend fun getLogDirectoryPath(): String
}
