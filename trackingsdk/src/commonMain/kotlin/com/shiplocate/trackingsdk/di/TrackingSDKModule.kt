package com.shiplocate.trackingsdk.di

import org.koin.core.module.Module

/**
 * DI модуль для TrackingSDK
 * Содержит регистрацию TrackingSDK как singleton для каждой платформы
 */
expect val trackingSDKModule: Module
