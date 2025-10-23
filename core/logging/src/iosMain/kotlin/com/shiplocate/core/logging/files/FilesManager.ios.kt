package com.shiplocate.core.logging.files

import io.ktor.utils.io.readText
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSInputStream
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.Foundation.inputStreamWithFileAtPath
import platform.Foundation.writeToFile

/**
 * iOS реализация FilesManager
 * Использует Foundation API для работы с файлами
 */
actual class FilesManager {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun createZipArchive(files: List<FileInfo>, archivePath: String): String {
        return withContext(Dispatchers.Default) {
            try {
                // Создаем директорию для архива
                val fileManager = NSFileManager.defaultManager
                val archiveUrl = NSURL.fileURLWithPath(archivePath)
                val parentDir = archiveUrl.URLByDeletingLastPathComponent
                if (parentDir != null) {
                    fileManager.createDirectoryAtURL(parentDir, true, null, null)
                }

                // Создаем простой архив (не ZIP) с объединенным содержимым
                val combinedContent = files.joinToString("\n---FILE_SEPARATOR---\n") { fileInfo ->
                    "FILE: ${fileInfo.name}\nCONTENT:\n${fileInfo.content.readText()}"
                }

                val combinedData = combinedContent.encodeToByteArray()
                memScoped {
                    val nsData = NSData.dataWithBytes(combinedData.refTo(0).getPointer(this@memScoped), combinedData.size.toULong())
                    nsData.writeToFile(archivePath, true)
                }

                archivePath
            } catch (e: Exception) {
                throw IllegalStateException("Failed to create archive: ${e.message}", e)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun deleteFile(filePath: String): Boolean {
        return withContext(Dispatchers.Default) {
            try {
                val fileManager = NSFileManager.defaultManager
                val url = NSURL.fileURLWithPath(filePath)
                fileManager.removeItemAtURL(url, null)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    actual suspend fun getFileSource(filePath: String): Source {
        return withContext(Dispatchers.Default) {
            val inputStream = NSInputStream.inputStreamWithFileAtPath(filePath)
                ?: throw IllegalStateException("Failed to open stream to file: $filePath")
            return@withContext inputStream.asSource().buffered()
        }
    }
}
