package com.shiplocate.presentation.model

/**
 * Enum для статуса Load в UI слое
 */
enum class LoadStatus {
    LOAD_STATUS_NOT_CONNECTED,    // 1
    LOAD_STATUS_CONNECTED,         // 1
    LOAD_STATUS_DISCONNECTED,      // 2
    LOAD_STATUS_REJECTED,          // 3
    LOAD_STATUS_UNKNOWN,           // Другое значение
}


