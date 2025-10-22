plugins {
    id("shiplocate.android.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.shiplocate.presentation"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Domain module
            implementation(project(":domain"))
            // Logging
            implementation(project(":core:logging"))

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

            // ViewModel lifecycle
            api(libs.lifecycle.viewmodel)
            api(libs.androidx.lifecycle.viewmodelCompose)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DateTime
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
