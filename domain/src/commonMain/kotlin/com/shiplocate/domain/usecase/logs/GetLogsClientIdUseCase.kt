package com.shiplocate.domain.usecase.logs

import com.shiplocate.domain.repository.AuthPreferencesRepository
import com.shiplocate.domain.repository.DeviceRepository
import com.shiplocate.domain.repository.LoadRepository
import com.shiplocate.domain.repository.PrefsRepository

class GetLogsClientIdUseCase(
    private val prefsRepository: PrefsRepository,
    private val deviceRepository: DeviceRepository,
    private val authPreferencesRepository: AuthPreferencesRepository,
    private val loadRepository: LoadRepository,
) {
    private fun sanitizeClientId(clientId: String): String {
        // Remove any path separators and dangerous characters
        return clientId.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
    }

    suspend operator fun invoke(): String {
        var clientId = if (authPreferencesRepository.hasSession()) {
            val phoneNumber = prefsRepository.getPhoneNumber()
            if (phoneNumber == null) {
                getFormatedClientId("NoPhone")
            } else {
                getFormatedClientId(phoneNumber)
            }
        } else {
            getFormatedClientId("NoPhone")
        }

        val activeLoadId = runCatching { loadRepository.getConnectedLoad()?.serverId }.getOrNull()
        if (activeLoadId != null) {
            clientId += "_load_$activeLoadId"
        }

        return sanitizeClientId(clientId)
    }

    private suspend fun getFormatedClientId(phoneNumber: String): String {
        val deviceInfo = getDeviceInfo()
        return "${phoneNumber}_${deviceInfo}"
    }

    private suspend fun getDeviceInfo(): String {
        val platform = deviceRepository.getPlatform().lowercase()
        val model = deviceRepository.getDeviceModel().lowercase().replace(" ", "-")
        val osVersion = deviceRepository.getOsVersion()

        return when (platform) {
            "android" -> {
                val apiLevel = deviceRepository.getApiLevel()
                val manufacturer = model
                "android_${manufacturer}_${model}_api_${apiLevel}"
            }

            "ios" -> {
                "ios_${model}_ios_${extractIosVersion(osVersion)}"
            }

            else -> {
                "${platform}_${model}_${osVersion}"
            }
        }
    }

    private fun extractIosVersion(osVersion: String): String {
        return try {
            val versionMatch = Regex("iOS (\\d+\\.\\d+)").find(osVersion)
            versionMatch?.groupValues?.get(1) ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
}
