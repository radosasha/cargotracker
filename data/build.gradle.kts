plugins {
    id("tracker.android.library")
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.tracker.data"
}

kotlin {
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
            
            // Room Database (нужен для работы с TrackerDatabase)
            implementation(libs.room.runtime)
            
            // Ktor (нужен для API классов в data)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            
            // DataStore (нужен для PrefsDataSourceImpl)
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)
            
            // Koin (только для DI в data слое)
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