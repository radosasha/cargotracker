package com.shiplocate.core.logging.files

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
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
                        zos.write(fileInfo.content)
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

    actual suspend fun readFile(filePath: String): ByteArray {
        return withContext(Dispatchers.IO) {
            File(filePath).readBytes()
        }
    }

    actual suspend fun fileExists(filePath: String): Boolean {
        return withContext(Dispatchers.IO) {
            File(filePath).exists()
        }
    }

    actual suspend fun createFileByteReadChannel(filePath: String): ByteReadChannel {
        return withContext(Dispatchers.IO) {
            val file = File(filePath)
            FileInputStream(file).toByteReadChannel()
        }
    }
}
