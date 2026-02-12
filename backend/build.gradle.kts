plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    application
}

group = "com.covoyage"
version = "1.0.0"

application {
    mainClass.set("com.covoyage.backend.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

// Repositories are managed in settings.gradle.kts

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")
    implementation("io.ktor:ktor-server-rate-limit-jvm")
    
    // Ktor Client (for Flutterwave API)
    implementation("io.ktor:ktor-client-core-jvm")
    implementation("io.ktor:ktor-client-cio-jvm")
    implementation("io.ktor:ktor-client-content-negotiation-jvm")
    implementation("io.ktor:ktor-client-logging-jvm")
    
    // Serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation(libs.kotlin.serialization)
    
    // MongoDB
    implementation(libs.mongodb.driver)
    implementation(libs.mongodb.bson)
    
    // Security
    implementation(libs.jbcrypt)
    
    // DateTime
    implementation(libs.kotlinx.datetime)
    
    // Logging
    implementation(libs.logback)
    
    // Configuration
    implementation(libs.dotenv)
    
    // Koin for DI
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    
    // Coroutines
    implementation(libs.coroutines.core)
    
    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation(kotlin("test-junit"))
    testImplementation("io.ktor:ktor-client-mock-jvm")
}

tasks {
    create("stage").dependsOn("installDist")
}

ktor {
    fatJar {
        archiveFileName.set("covoyage-backend.jar")
    }
}
