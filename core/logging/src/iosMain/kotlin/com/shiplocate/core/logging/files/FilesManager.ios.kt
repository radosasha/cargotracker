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
    private val logDirectoryPath: String = "/tmp/logs"

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

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun writeToFile(filePath: String, content: String) {
        withContext(Dispatchers.Default) {
            try {
                // Создаем директорию если не существует
                val fileManager = NSFileManager.defaultManager
                val fileUrl = NSURL.fileURLWithPath(filePath)
                val parentDir = fileUrl.URLByDeletingLastPathComponent
                if (parentDir != null) {
                    fileManager.createDirectoryAtURL(parentDir, true, null, null)
                }

                // Записываем содержимое в файл
                val data = content.encodeToByteArray()
                memScoped {
                    val nsData = NSData.dataWithBytes(data.refTo(0).getPointer(this@memScoped), data.size.toULong())
                    nsData.writeToFile(filePath, true)
                }
            } catch (e: Exception) {
                println("Failed to write to file $filePath: ${e.message}")
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getFileSize(filePath: String): Long {
        return withContext(Dispatchers.Default) {
            try {
                val fileManager = NSFileManager.defaultManager
                val attributes = fileManager.attributesOfItemAtPath(filePath, null)
                attributes?.get(platform.Foundation.NSFileSize) as? Long ?: 0L
            } catch (e: Exception) {
                0L
            }
        }
    }

    actual suspend fun fileExists(filePath: String): Boolean {
        return withContext(Dispatchers.Default) {
            NSFileManager.defaultManager.fileExistsAtPath(filePath)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun listFiles(directoryPath: String): List<String> {
        return withContext(Dispatchers.Default) {
            try {
                val fileManager = NSFileManager.defaultManager
                val contents = fileManager.contentsOfDirectoryAtPath(directoryPath, null)
                contents?.mapNotNull { it as? String } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun createDirectoryIfNotExists(directoryPath: String) {
        withContext(Dispatchers.Default) {
            try {
                val fileManager = NSFileManager.defaultManager
                val url = NSURL.fileURLWithPath(directoryPath)
                fileManager.createDirectoryAtURL(url, true, null, null)
            } catch (e: Exception) {
                println("Failed to create directory $directoryPath: ${e.message}")
            }
        }
    }

    actual suspend fun getLogDirectoryPath(): String {
        return logDirectoryPath
    }
}
