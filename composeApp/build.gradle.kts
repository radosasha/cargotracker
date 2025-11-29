plugins {
    id("shiplocate.android.application")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktlint)

    // Add the Google services Gradle plugin
    alias(libs.plugins.google.services)
}

ktlint {
    version.set("1.2.1")
    debug.set(false)
    verbose.set(false)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    enableExperimentalRules.set(false)

    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
        include("**/kotlin/**")
    }

}

android {
    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // Koin Android
            implementation(libs.koin.android)

            // Coroutines Android
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.lifecycle.service)
            implementation(libs.play.services.location)
        }

        commonMain.dependencies {
            // Modules
            implementation(project(":domain"))
            implementation(project(":data"))
            implementation(project(":presentation"))
            implementation(project(":trackingsdk"))
            implementation(project(":core:logging"))

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

            // ViewModel lifecycle
            api(libs.lifecycle.viewmodel)
            api(libs.androidx.lifecycle.viewmodelCompose)

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

    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))

    // Firebase Messaging for push notifications
    implementation(libs.firebase.messaging)

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries
}
