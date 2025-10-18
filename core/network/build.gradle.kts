plugins {
    id("shiplocate.android.library.core")
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktlint)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Kotlin
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            // Ktor
            implementation(libs.ktor.client.android)
        }

        iosMain.dependencies {
            // Ktor
            implementation(libs.ktor.client.ios)
        }
    }
}

android {
    namespace = "com.shiplocate.core.network"
}
