package com.shiplocate.di

import com.shiplocate.IOSPermissionManagerImpl
import com.shiplocate.data.datasource.PermissionManager
import org.koin.dsl.module

/**
 * iOS-специфичный модуль для composeApp (ViewController scope)
 */
val iosModule =
    module {

        // iOS Permission Requester для domain слоя
        single<PermissionManager> { IOSPermissionManagerImpl() }
    }
