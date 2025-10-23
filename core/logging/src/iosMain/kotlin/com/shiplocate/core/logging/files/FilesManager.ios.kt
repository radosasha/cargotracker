package com.shiplocate.core.logging.files

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.Source
import kotlinx.io.buffered
import java.io.ByteArrayInputStream

/**
 * iOS реализация FilesManager
 * Пока используем заглушки, так как для iOS нужна более сложная реализация с ZIP архивированием
 */
actual class FilesManager {
    actual suspend fun createZipArchive(files: List<FileInfo>, archivePath: String): String {
        return withContext(Dispatchers.Main) {
            // TODO: Реализовать ZIP архивирование для iOS
            // Пока возвращаем заглушку
            archivePath
        }
    }

    actual suspend fun deleteFile(filePath: String): Boolean {
        return withContext(Dispatchers.Main) {
            // TODO: Реализовать удаление файла для iOS
            // Пока возвращаем заглушку
            true
        }
    }

    actual suspend fun getFileSource(filePath: String): Source {
        return withContext(Dispatchers.Main) {
            // TODO: Реализовать чтение файла для iOS
            // Пока возвращаем заглушку
            ByteArrayInputStream(ByteArray(0)).buffered().source()
        }
    }
}
