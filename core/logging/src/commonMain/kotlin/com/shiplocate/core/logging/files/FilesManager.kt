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


    suspend fun getFileSource(filePath: String) : Source
}

/**
 * Информация о файле для архивирования
 */
data class FileInfo(
    val name: String,
    val content: Source,
)
