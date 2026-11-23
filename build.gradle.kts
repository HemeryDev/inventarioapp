buildscript {
    configurations.classpath {
        resolutionStrategy {
            force("org.jetbrains:annotations:23.0.0")
        }
    }
}

// Archivo: build.gradle.kts (Project: InventarioApp)
plugins {
    // Mantén los alias que ya te generó Android Studio arriba (ej. android.application, kotlin.compose)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Agrega estos 4 plugins EXACTAMENTE con "apply false":
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
    id("com.google.gms.google-services") version "4.6.0" apply false
    id("com.google.devtools.ksp") version "2.3.12" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10" apply false
}