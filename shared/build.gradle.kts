@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    // ... ios targets ...
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                
                // Coroutines
                implementation(libs.coroutines.core)
                
                // Ktor Client
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content-negotiation)
                implementation(libs.ktor.serialization.kotlinx-json)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.auth)
                
                // Serialization
                implementation(libs.kotlin.serialization)
                
                // DateTime
                implementation(libs.kotlinx.datetime)
                
                // Koin
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.android)
                implementation(libs.androidx.startup)
                
                // Android specific
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.activity.compose)
                
                // Google Play Services Location
                implementation(libs.google.play.services.location)
            }
        }
        
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}

android {
    namespace = "com.covoyage.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}


