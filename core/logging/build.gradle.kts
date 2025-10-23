plugins {
    id("shiplocate.android.library.core")
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktlint)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            implementation(libs.ktor.io)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.io)
            implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.4.1")
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.koin.android)
        }

        iosMain.dependencies {
            // iOS использует встроенные Foundation API
            implementation(libs.ktor.client.ios)
        }
    }
}

android {
    namespace = "com.shiplocate.core.logging"
}
