import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.tracker.data"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Data"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            // Domain module
            implementation(project(":domain"))
            
            // Core modules
            implementation(project(":core:database"))
            implementation(project(":core:network"))
            implementation(project(":core:datastore"))
            
            // Kotlin
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            
            // Room Database
            implementation(libs.room.runtime)
            
            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            
            // DataStore
            implementation("androidx.datastore:datastore:1.1.7")
            implementation("androidx.datastore:datastore-preferences:1.1.7")
            
            // Koin
            implementation(libs.koin.core)
        }
        
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        
        iosMain.dependencies {
            // iOS specific dependencies if needed
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}