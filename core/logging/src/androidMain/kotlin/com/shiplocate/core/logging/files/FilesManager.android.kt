package com.shiplocate.core.logging.files

import io.ktor.utils.io.streams.asInput
import io.ktor.utils.io.streams.inputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.Source
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Android реализация FilesManager
 */
actual class FilesManager {
    actual suspend fun createZipArchive(files: List<FileInfo>, archivePath: String): String {
        return withContext(Dispatchers.IO) {
            val file = File(archivePath)
            file.parentFile?.mkdirs()

            FileOutputStream(file).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    files.forEach { fileInfo ->
                        val entry = ZipEntry(fileInfo.name)
                        zos.putNextEntry(entry)
                        fileInfo.content.inputStream().use { input ->
                            input.copyTo(zos)
                        }
                        zos.closeEntry()
                    }
                }
            }
            archivePath
        }
    }

    actual suspend fun deleteFile(filePath: String): Boolean {
        return withContext(Dispatchers.IO) {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        }
    }

    actual suspend fun getFileSource(filePath: String): Source {
        return withContext(Dispatchers.IO) {
            val file = File(filePath)
            val bytes = file.readBytes()
            bytes.inputStream().buffered().asInput()
        }
    }
}
