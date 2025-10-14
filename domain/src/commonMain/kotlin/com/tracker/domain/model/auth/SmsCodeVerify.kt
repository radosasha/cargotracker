package com.tracker.domain.model.auth

/**
 * Domain model for SMS code verification
 */
data class SmsCodeVerify(
    val phone: String,
    val code: String,
    val deviceInfo: String? = null
)
