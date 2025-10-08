plugins {
    id("tracker.android.application")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            
            // Koin Android
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
            
            // Coroutines Android
            implementation(libs.kotlinx.coroutines.android)
        }
        
        commonMain.dependencies {
            // Modules
            implementation(project(":domain"))
            implementation(project(":data"))
            implementation(project(":presentation"))
            
            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            
            // Official JetBrains Navigation
            implementation(libs.androidx.navigation.compose)
            
            // Koin Core
            implementation(libs.koin.core)
            
            // DateTime
            implementation(libs.kotlinx.datetime)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

