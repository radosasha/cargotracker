import com.android.build.gradle.LibraryExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
}

configure<LibraryExtension> {
    compileSdk = AndroidConfig.COMPILE_SDK
    
    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK
    }
    
    compileOptions {
        sourceCompatibility = AndroidConfig.JAVA_VERSION_CORE
        targetCompatibility = AndroidConfig.JAVA_VERSION_CORE
    }
}

configure<KotlinMultiplatformExtension> {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = AndroidConfig.JVM_TARGET_CORE
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            isStatic = true
        }
    }
}
