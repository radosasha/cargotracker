package com.shiplocate.domain.usecase.logs

/**
 * Use Case для отправки всех лог-файлов на сервер через архив
 */
expect class SendAllLogsUseCase {

    /**
     * Отправляет все лог-файлы на сервер через архив
     * @param clientId идентификатор клиента
     */
    suspend operator fun invoke(clientId: String): Result<Unit>
}

