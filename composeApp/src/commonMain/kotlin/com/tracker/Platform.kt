package com.tracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform