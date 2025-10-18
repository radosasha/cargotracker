plugins {
    id("tracker.android.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.tracker.presentation"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Domain module
            implementation(project(":domain"))

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Lifecycle
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Official JetBrains Navigation
            implementation(libs.androidx.navigation.compose)

            // Koin
            implementation(libs.koin.core)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DateTime
            implementation(libs.kotlinx.datetime)
        }

        androidMain.dependencies {
            implementation(libs.koin.compose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
