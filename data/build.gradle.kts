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
            
            // Serialization
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            
            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            
            // Koin
            implementation(libs.koin.core)
        }
        
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            implementation(libs.koin.android)
        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.ios)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}