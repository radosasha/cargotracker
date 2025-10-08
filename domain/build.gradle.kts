plugins {
    id("tracker.android.library")
}

android {
    namespace = "com.tracker.domain"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // DateTime (for Instant in models)
            implementation(libs.kotlinx.datetime)
            
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
