plugins {
    id("shiplocate.android.library.core")
    alias(libs.plugins.ktlint)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Kotlin
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // DataStore
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            // Android
            implementation(libs.androidx.core.ktx)
        }

        iosMain.dependencies {
            // iOS specific dependencies if needed
        }
    }
}

android {
    namespace = "com.shiplocate.core.datastore"
}
