package com.shiplocate.di

import com.shiplocate.IOSPermissionRequesterImpl
import com.shiplocate.data.datasource.PermissionRequester
import org.koin.dsl.module

/**
 * iOS-специфичный модуль для composeApp (ViewController scope)
 */
val iosModule =
    module {

        // iOS Permission Requester для domain слоя
        single<PermissionRequester> { IOSPermissionRequesterImpl() }
    }
