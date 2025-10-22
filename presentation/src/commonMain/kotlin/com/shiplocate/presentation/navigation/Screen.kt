package com.shiplocate.presentation.navigation

/**
 * Определение экранов приложения
 * Использует строковые маршруты для кроссплатформенной совместимости
 */
object Screen {
    const val ENTER_PHONE = "enter_phone"
    const val ENTER_PIN = "enter_pin/{phone}"
    const val LOADS = "loads"
    const val HOME = "home/{loadId}"
    const val LOGS = "logs"

    fun enterPin(phone: String) = "enter_pin/$phone"

    fun home(loadId: String) = "home/$loadId"
}
