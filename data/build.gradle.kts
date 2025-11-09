plugins {
    id("shiplocate.android.library")
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.shiplocate.data"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Domain module
            implementation(project(":domain"))
            
            // TrackingSDK module
            implementation(project(":trackingsdk"))

            // Core modules
            implementation(project(":core:database")) // Room + SQLite
            implementation(project(":core:network")) // Ktor + HttpClient
            implementation(project(":core:datastore")) // DataStore
            implementation(project(":core:logging")) // Logging

            // Kotlin
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            // DataStore API (needed for PrefsDataSourceImpl)
            // core:datastore provides only DataStoreProvider, not DataStore API
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)

            // Ktor API (needed for FlespiLocationApi and OsmAndLocationApi)
            // core:network provides only HttpClientProvider, not Ktor API
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            // Room API (needed for LocationLocalDataSourceImpl)
            // core:database provides only TrackerDatabase, not Room API
            implementation(libs.room.runtime)

            // Koin (только для DI в data слое)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
            
            // Google Play Services for FusedLocationProviderClient
            implementation(libs.play.services.location)
        }

        iosMain.dependencies {
            // iOS specific dependencies if needed
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
