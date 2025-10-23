package com.shiplocate.domain.usecase

import com.shiplocate.domain.repository.DeviceRepository

/**
 * Use Case для получения информации об устройстве
 * Использует DeviceRepository для получения данных об устройстве
 */
class GetDeviceInfoUseCase(
    private val deviceRepository: DeviceRepository,
) {
    /**
     * Получает полную информацию об устройстве в формате строки
     * @return String - информация об устройстве в формате "Platform/OS/Model"
     */
    suspend operator fun invoke(): String {
        return deviceRepository.getDeviceInfo()
    }
}
