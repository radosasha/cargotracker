package com.shiplocate.core.logging.files

import kotlinx.io.Source

/**
 * Expect класс для работы с файлами на разных платформах
 */
expect class FilesManager {
    /**
     * Создает ZIP архив из списка файлов
     * @param files список файлов для архивирования
     * @param archivePath путь к создаваемому архиву
     * @return путь к созданному архиву
     */
    suspend fun createZipArchive(files: List<FileInfo>, archivePath: String): String

    /**
     * Удаляет файл по указанному пути
     * @param filePath путь к файлу для удаления
     * @return true если файл был удален, false если файл не найден
     */
    suspend fun deleteFile(filePath: String): Boolean

    /**
     * Получает Source для чтения файла
     * @param filePath путь к файлу
     * @return Source для чтения файла
     */
    suspend fun getFileSource(filePath: String): Source

    /**
     * Записывает строку в файл (добавляет к существующему содержимому)
     * @param filePath путь к файлу
     * @param content содержимое для записи
     */
    suspend fun writeToFile(filePath: String, content: String)

    /**
     * Получает размер файла в байтах
     * @param filePath путь к файлу
     * @return размер файла в байтах
     */
    suspend fun getFileSize(filePath: String): Long

    /**
     * Проверяет существование файла
     * @param filePath путь к файлу
     * @return true если файл существует
     */
    suspend fun fileExists(filePath: String): Boolean

    /**
     * Получает список файлов в директории
     * @param directoryPath путь к директории
     * @return список имен файлов
     */
    suspend fun listFiles(directoryPath: String): List<String>
}

/**
 * Информация о файле для архивирования
 */
data class FileInfo(
    val name: String,
    val path: String,
    val content: Source,
)
