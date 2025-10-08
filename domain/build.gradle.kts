plugins {
    id("tracker.android.library")
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.tracker.domain"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Serialization
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
