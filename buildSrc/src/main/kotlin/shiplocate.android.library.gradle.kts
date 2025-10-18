import com.android.build.gradle.LibraryExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
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
