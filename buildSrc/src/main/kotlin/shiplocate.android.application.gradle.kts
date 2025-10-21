import com.android.build.api.dsl.ApplicationExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.multiplatform")
}

configure<ApplicationExtension> {
    namespace = AndroidConfig.NAMESPACE
    compileSdk = AndroidConfig.COMPILE_SDK
    
    defaultConfig {
        applicationId = AndroidConfig.APPLICATION_ID
        minSdk = AndroidConfig.MIN_SDK
        targetSdk = AndroidConfig.TARGET_SDK
        versionCode = AndroidConfig.VERSION_CODE
        versionName = AndroidConfig.VERSION_NAME
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    
    compileOptions {
        sourceCompatibility = AndroidConfig.JAVA_VERSION
        targetCompatibility = AndroidConfig.JAVA_VERSION
    }
}

configure<KotlinMultiplatformExtension> {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(AndroidConfig.JVM_TARGET))
        }
    }
    
    // iOS targets с framework
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            // baseName по умолчанию = имя модуля
            // Можно переопределить в конкретном модуле через:
            // kotlin.targets.withType<KotlinNativeTarget> { binaries.framework { baseName = "..." } }
            baseName = "ComposeApp"
            export("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:2.9.4")
            isStatic = true
        }
    }
}
