package com.shiplocate.core.logging.appender

import com.shiplocate.core.logging.LogEntry
import com.shiplocate.core.logging.LoggingConfig
import com.shiplocate.core.logging.LogsSettings
import com.shiplocate.core.logging.files.FileAttributes
import com.shiplocate.core.logging.files.FilesManager
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.Source

/**
 * Appender для записи логов в файлы с поддержкой ротации
 */
class FileAppender(
    private val config: LoggingConfig,
    private val filesManager: FilesManager,
    private val logsSettings: LogsSettings,
) : LogAppender {
    private var currentFile: String? = null
    private var currentFileSize: Long = 0
    private var currentHour: Int = -1

    override suspend fun append(entry: LogEntry) {
        val fileName = getCurrentFileName()
        val formattedMessage = formatLogEntry(entry)

        try {
            // Проверяем, нужно ли создать новый файл
            if (shouldCreateNewFile(fileName)) {
                currentFile = fileName
                currentFileSize = 0
            }

            // Записываем в файл
            val filePath = "${logsSettings.getLogsDirectory()}/$fileName"
            filesManager.writeToFile(filePath, formattedMessage)

            // Обновляем размер файла
            currentFileSize += formattedMessage.length.toLong()
        } catch (e: Exception) {
            // Если не можем записать в файл, выводим в консоль
            println("Failed to write log to file: ${e.message}")
        }
    }

    /**
     * Получает имя текущего файла для записи
     */
    private fun getCurrentFileName(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dateStr = "${now.dayOfMonth.toString().padStart(2, '0')}.${now.monthNumber.toString().padStart(2, '0')}.${now.year}"

        return if (config.fileHours) {
            val hour = now.hour
            val baseFileName = "${dateStr}_${hour.toString().padStart(2, '0')}.txt"
            // Для упрощения пока возвращаем базовое имя файла
            baseFileName
        } else {
            val timeStr = "${now.minute.toString().padStart(2, '0')}.${now.hour.toString().padStart(2, '0')}.${
                now.second.toString().padStart(2, '0')
            }"
            "${dateStr}_$timeStr.txt"
        }
    }


    /**
     * Проверяет, нужно ли создать новый файл
     */
    private suspend fun shouldCreateNewFile(fileName: String): Boolean {
        // Если это новый файл
        if (currentFile == null) return false
        if (currentFile != fileName) {
            return true
        }

        // Если файл превысил максимальный размер
        if (currentFileSize >= config.maxFileSize) {
            return true
        }

        // Если включена ротация по часам и час изменился
        if (config.fileHours) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            if (currentHour != now.hour) {
                currentHour = now.hour
                return true
            }
        }

        return false
    }

    /**
     * Форматирует запись лога для файла
     */
    private fun formatLogEntry(entry: LogEntry): String {
        return buildString {
            append("[${entry.formattedTime}] ")
            append("[${entry.level.name}] ")
            append("[${entry.category.displayName}] ")
            append(entry.fullMessage)

            // Добавляем стектрейс если есть исключение
            entry.throwable?.let { throwable ->
                append("\nStack trace:")
                append("\n${throwable.stackTraceToString()}")
            }
            append("\n")
        }
    }

    /**
     * Получает список всех лог-файлов
     */
    suspend fun getLogFiles(): List<FileAttributes> {
        return filesManager.listFiles(logsSettings.getLogsDirectory())
    }

    /**
     * Удаляет лог-файл
     */
    suspend fun deleteLogFile(fileName: String): Boolean {
        val filePath = "${logsSettings.getLogsDirectory()}/$fileName"
        return filesManager.deleteFile(filePath)
    }

    /**
     * Получает размер лог-файла
     */
    suspend fun getLogFileSize(fileName: String): Long {
        val filePath = "${logsSettings.getLogsDirectory()}/$fileName"
        return filesManager.getFileSize(filePath)
    }

    /**
     * Получает содержимое лог-файла
     */
    suspend fun getLogFileSource(fileName: String): Source {
        val filePath = "${logsSettings.getLogsDirectory()}/$fileName"
        return filesManager.getFileSource(filePath)
    }

    /**
     * Получает путь к директории с логами
     */
    suspend fun getLogDirectoryPath(): String {
        return logsSettings.getLogsDirectory()
    }
}
