package com.shiplocate.domain.model.load

/**
 * Enum для статуса Load в Domain слое
 * Соответствует константам на сервере (Load.java)
 */
enum class LoadStatus() {
    LOAD_STATUS_NOT_CONNECTED,
    LOAD_STATUS_CONNECTED,
    LOAD_STATUS_DISCONNECTED,
    LOAD_STATUS_REJECTED,
    LOAD_STATUS_UNKNOWN;
}

