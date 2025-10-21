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

    // Дополнительные настройки для игнорирования правил
    disabledRules.set(
        setOf(
            "function-naming",
            "class-naming",
            "discouraged-comment-location",
            "no-empty-first-line-in-class-body",
            "blank-line-before-declaration",
            "wrapping",
            "parameter-list-wrapping",
            "multiline-expression-wrapping",
        ),
    )
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
            implementation(libs.koin.compose)

            // Coroutines Android
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.lifecycle.service)
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

    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries
}
