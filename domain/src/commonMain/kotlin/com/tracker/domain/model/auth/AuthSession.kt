package com.tracker.domain.model.auth

/**
 * Domain model for authenticated session
 */
data class AuthSession(
    val token: String,
    val user: AuthUser
)
