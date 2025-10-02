package com.tracker

import android.content.Context
import android.app.Application

/**
 * Провайдер Application Context
 */
object ApplicationContextProvider {
    private var applicationContext: Context? = null
    
    fun init(context: Context) {
        println("ApplicationContextProvider.init() called with context: ${context.javaClass}")
        applicationContext = context.applicationContext
        println("ApplicationContextProvider initialized successfully")
    }
    
    fun getContext(): Context {
        val context = applicationContext
        if (context == null) {
            println("ApplicationContextProvider.getContext() failed - context is null")
            throw IllegalStateException("Application context not initialized. Call ApplicationContextProvider.init() first.")
        }
        println("ApplicationContextProvider.getContext() returning: ${context.javaClass}")
        return context
    }
}

/**
 * Провайдер Activity Context (управляется через Koin)
 */
class ActivityContextProvider {
    private var activityContext: Context? = null
    
    fun setContext(context: Context) {
        println("ActivityContextProvider.setContext() called with context: ${context.javaClass}")
        activityContext = context
    }
    
    fun getContext(): Context {
        val context = activityContext
        if (context == null) {
            println("ActivityContextProvider.getContext() failed - context is null")
            throw IllegalStateException("Activity context not set.")
        }
        println("ActivityContextProvider.getContext() returning: ${context.javaClass}")
        return context
    }
    
    fun clearContext() {
        println("ActivityContextProvider.clearContext() called")
        activityContext = null
    }
}
