package com.shiplocate.presentation.model

/**
 * Enum для статуса Load в UI слое
 */
enum class LoadStatus {
    LOAD_STATUS_NOT_CONNECTED,    // 0
    LOAD_STATUS_CONNECTED,         // 1
    LOAD_STATUS_DISCONNECTED,      // 2
    LOAD_STATUS_REJECTED,          // 3
    LOAD_STATUS_UNKNOWN,           // Другое значение
}

/**
 * Конвертирует Int статус в LoadStatus enum
 */
fun Int.toLoadStatus(): LoadStatus {
    return when (this) {
        0 -> LoadStatus.LOAD_STATUS_NOT_CONNECTED
        1 -> LoadStatus.LOAD_STATUS_CONNECTED
        2 -> LoadStatus.LOAD_STATUS_DISCONNECTED
        3 -> LoadStatus.LOAD_STATUS_REJECTED
        else -> LoadStatus.LOAD_STATUS_UNKNOWN
    }
}

