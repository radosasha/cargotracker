package com.shiplocate.domain.usecase.logs

actual class SendAllLogsUseCase {

    /**
     * Отправляет все лог-файлы на сервер через архив
     * @param clientId идентификатор клиента
     */
    actual suspend operator fun invoke(clientId: String): Result<Unit> {
        // TODO: Implement iOS version
        return Result.failure(NotImplementedError("SendAllLogsUseCase is not implemented for iOS"))
    }
}

