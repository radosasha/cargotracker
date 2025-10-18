plugins {
    id("tracker.android.application")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktlint)
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
}
