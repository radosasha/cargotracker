plugins {
    id("shiplocate.android.library")
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.shiplocate.domain"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // DateTime (for Instant in models)
            implementation(libs.kotlinx.datetime)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            
            // Logging
            implementation(project(":core:logging"))
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
