package com.tracker.domain.model.auth

/**
 * Supported countries for phone authentication
 */
enum class Country(
    val code: String,
    val dialCode: String,
    val flag: String,
    val phoneLength: Int, // digits after country code
) {
    US(
        code = "US",
        dialCode = "+1",
        flag = "🇺🇸",
        phoneLength = 10,
    ),
    CANADA(
        code = "CA",
        dialCode = "+1",
        flag = "🇨🇦",
        phoneLength = 10,
    ),
    MEXICO(
        code = "MX",
        dialCode = "+52",
        flag = "🇲🇽",
        phoneLength = 10,
    ),
    ;

    val totalLength: Int
        get() = dialCode.length + phoneLength
}
