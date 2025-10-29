plugins {
    id("shiplocate.android.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.shiplocate.trackingsdk"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            
            // Зависимости от наших модулей
            implementation(project(":domain"))
            implementation(project(":core:logging"))
            implementation(project(":core:network"))
        }
        
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.activity.compose)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.koin.compose)
            
            // Android Service dependencies
            implementation("androidx.lifecycle:lifecycle-service:2.7.0")
            implementation("androidx.core:core-ktx:1.12.0")
            
            // Google Play Services for ActivityRecognition
            implementation("com.google.android.gms:play-services-location:21.0.1")
        }
        
        iosMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
