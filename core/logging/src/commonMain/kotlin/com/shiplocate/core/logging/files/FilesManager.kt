package com.shiplocate.core.logging.files

import io.ktor.utils.io.ByteReadChannel

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
     * Читает содержимое файла как ByteArray
     * @param filePath путь к файлу
     * @return содержимое файла
     */
    suspend fun readFile(filePath: String): ByteArray

    /**
     * Проверяет существование файла
     * @param filePath путь к файлу
     * @return true если файл существует
     */
    suspend fun fileExists(filePath: String): Boolean

    /**
     * Создает ByteReadChannel для чтения файла
     * @param filePath путь к файлу
     * @return ByteReadChannel для чтения файла
     */
    suspend fun createFileByteReadChannel(filePath: String): ByteReadChannel
}

/**
 * Информация о файле для архивирования
 */
data class FileInfo(
    val name: String,
    val content: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FileInfo

        if (name != other.name) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}
