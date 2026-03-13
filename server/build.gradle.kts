import java.util.Properties

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

application {
    mainClass.set("covoyage.server.ApplicationKt")
}

// Load local.properties
val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

tasks.withType<JavaExec> {
    // Pass MongoDB properties from local.properties to the environment
    localProperties.getProperty("MONGO_URI")?.let { environment("MONGO_URI", it) }
    localProperties.getProperty("MONGO_DB")?.let { environment("MONGO_DB", it) }
}

dependencies {
    // Ktor server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.server.callLogging)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.sessions)
    implementation(libs.ktor.server.html.builder)
    implementation(libs.ktor.serialization.json)

    // Ktor client (for calling Flutterwave API)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.client.logging)

    // MongoDB
    implementation(libs.mongodb.driver.kotlin.coroutine)
    implementation(libs.mongodb.bson.kotlinx)

    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)

    // Logging
    implementation(libs.logback.classic)

    // Firebase Admin (for FCM push notifications)
    implementation(libs.firebase.admin)

    // WebSocket (for chat)
    implementation(libs.ktor.server.websockets)

    // Security — BCrypt password hashing
    implementation(libs.jbcrypt)
}

tasks.register<JavaExec>("testMongo") {
    mainClass.set("covoyage.server.database.TestConnectionKt")
    classpath = sourceSets["main"].runtimeClasspath
}
