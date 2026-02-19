plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

application {
    mainClass.set("covoyage.server.ApplicationKt")
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
