package com.shiplocate.domain.usecase.logs

import com.shiplocate.domain.model.logs.LogFile

/**
 * Use Case для отправки лог-файлов на сервер
 */
expect class SendLogsUseCase {

    /**
     * Отправляет выбранные лог-файлы на сервер через архив
     */
    suspend operator fun invoke(clientId: String, files: List<LogFile>): Result<Unit>
}
