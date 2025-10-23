package com.shiplocate.core.logging.appender

import android.content.Context
import com.shiplocate.core.logging.LogEntry
import com.shiplocate.core.logging.LoggingConfig
import io.ktor.utils.io.streams.asInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.Source
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * Android-специфичная реализация FileAppender
 */
actual class FileAppender(
    private val config: LoggingConfig,
    private val context: Context,
) : LogAppender {
    private var currentFile: String? = null
    private var currentFileSize: Long = 0
    private var currentHour: Int = -1

    private val logDirectory: File by lazy {
        val dir = File(context.filesDir, config.logDirectory)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    actual override suspend fun append(entry: LogEntry) {
        val fileName = getCurrentFileName()
        val formattedMessage = formatLogEntry(entry)

        try {
            // Проверяем, нужно ли создать новый файл
            if (shouldCreateNewFile(fileName)) {
                currentFile = fileName
                currentFileSize = 0
            }

            // Записываем в файл
            writeToFile(fileName, formattedMessage)

            // Обновляем размер файла
            currentFileSize += formattedMessage.toByteArray().size.toLong()
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
            getNextAvailableFileName(baseFileName)
        } else {
            val timeStr = "${
                now.minute.toString().padStart(
                    2,
                    '0',
                )
            }.${now.hour.toString().padStart(2, '0')}.${now.second.toString().padStart(2, '0')}"
            "${dateStr}_$timeStr.txt"
        }
    }

    /**
     * Получает следующее доступное имя файла с учетом размера
     */
    private fun getNextAvailableFileName(baseFileName: String): String {
        if (!config.fileHours) {
            return baseFileName
        }

        val baseName = baseFileName.substringBeforeLast(".")
        val extension = baseFileName.substringAfterLast(".")

        var counter = 1
        var fileName = baseFileName

        while (shouldCreateNewFile(fileName)) {
            fileName = "$baseName($counter).$extension"
            counter++
        }

        return fileName
    }

    /**
     * Проверяет, нужно ли создать новый файл
     */
    private fun shouldCreateNewFile(fileName: String): Boolean {
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
     * Записывает сообщение в файл
     */
    private suspend fun writeToFile(
        fileName: String,
        message: String,
    ) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(logDirectory, fileName)
                FileWriter(file, true).use { writer ->
                    writer.append(message)
                    writer.flush()
                }
            } catch (e: IOException) {
                // Логируем ошибку записи в файл
                println("Failed to write to log file $fileName: ${e.message}")
            }
        }
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

    actual suspend fun getLogFiles(): List<String> {
        return withContext(Dispatchers.IO) {
            logDirectory.listFiles()?.map { it.name } ?: emptyList()
        }
    }

    actual suspend fun deleteLogFile(fileName: String): Boolean {
        return withContext(Dispatchers.IO) {
            val file = File(logDirectory, fileName)
            file.delete()
        }
    }

    actual suspend fun getLogFileSize(fileName: String): Long {
        return withContext(Dispatchers.IO) {
            val file = File(logDirectory, fileName)
            file.length()
        }
    }

    actual suspend fun getLogFileSource(fileName: String): Source {
        return withContext(Dispatchers.IO) {
            val file = File(logDirectory, fileName)
            file.inputStream().buffered().asInput()
        }
    }

    actual suspend fun getLogDirectoryPath(): String {
        return logDirectory.absolutePath
    }
}
