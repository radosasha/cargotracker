package com.tracker.domain.model.auth

/**
 * Domain model for authentication token
 */
data class AuthToken(
    val token: String,
    val user: AuthUser
)





