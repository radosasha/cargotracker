package com.shiplocate.data.network.api

import com.shiplocate.domain.model.logs.LogFile

/**
 * Интерфейс API для отправки логов на сервер
 */
interface LogsApi {

    /**
     * Отправляет архив с логами на сервер
     * @param archivePath путь к архиву для отправки
     * @param clientId идентификатор клиента
     */
    suspend fun sendLogArchive(archivePath: String, clientId: String): Result<Unit>

    /**
     * Отправляет список файлов логов на сервер
     * @param files список путей к файлам для отправки
     * @param clientId идентификатор клиента
     */
    suspend fun sendLogFiles(files: List<String>, clientId: String): Result<Unit>
}
