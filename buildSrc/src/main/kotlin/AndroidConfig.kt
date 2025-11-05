import org.gradle.api.JavaVersion

object AndroidConfig {
    const val COMPILE_SDK = 36
    const val MIN_SDK = 26
    const val TARGET_SDK = 36
    
    const val VERSION_CODE = 5
    const val VERSION_NAME = "1.0.5"
    
    // Java версии
    val JAVA_VERSION = JavaVersion.VERSION_11
    const val JVM_TARGET = "11"
    
    // Для core модулей (если нужна обратная совместимость)
    val JAVA_VERSION_CORE = JavaVersion.VERSION_1_8
    const val JVM_TARGET_CORE = "1.8"
    
    // Application
    const val APPLICATION_ID = "com.shiplocate"
    const val NAMESPACE = "com.shiplocate"
}
