package com.shiplocate.data.datasource

/**
 * Интерфейс удаленного источника данных для работы с логами
 */
interface LogsRemoteDataSource {

    /**
     * Отправляет архив с логами на сервер
     * @param archivePath путь к архиву для отправки
     * @param clientId идентификатор клиента
     */
    suspend fun sendLogArchive(archivePath: String, clientId: String): Result<Unit>
    suspend fun sendLog(files: List<String>, clientId: String): Result<Unit>
}
