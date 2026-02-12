package com.covoyage.backend

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.covoyage.backend.config.DatabaseConfig
import com.covoyage.backend.config.JwtConfig
import com.covoyage.backend.controllers.*
import com.covoyage.backend.routes.*
import com.covoyage.backend.services.*
import com.covoyage.backend.utils.ApiResponse
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

fun main() {
    val dotenv = dotenv { ignoreIfMissing = true }
    val port = dotenv["PORT"]?.toIntOrNull() ?: 5000
    val host = dotenv["HOST"] ?: "0.0.0.0"

    embeddedServer(Netty, port = port, host = host, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val dotenv = dotenv { ignoreIfMissing = true }

    // Initialize database synchronously at startup
    runBlocking {
        DatabaseConfig.init()
    }

    // Initialize repositories
    val userRepository = UserRepository()
    val rideRepository = RideRepository()
    val bookingRepository = BookingRepository()
    val paymentRepository = PaymentRepository()
    val ratingRepository = RatingRepository()

    // Initialize services
    val flutterwaveService = FlutterwaveService()

    // Initialize controllers
    val authController = AuthController(userRepository)
    val rideController = RideController(rideRepository, userRepository)
    val bookingController = BookingController(bookingRepository, rideRepository, userRepository)
    val paymentController = PaymentController(paymentRepository, bookingRepository, flutterwaveService)
    val ratingController = RatingController(ratingRepository, bookingRepository, userRepository)

    // Configure plugins
    configureSerialization()
    configureSecurity()
    configureHTTP(dotenv)
    configureMonitoring()
    configureRouting(authController, rideController, bookingController, paymentController, ratingController)
    configureStatusPages()

    // Shutdown hook
    environment.monitor.subscribe(ApplicationStopped) {
        DatabaseConfig.close()
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(JwtConfig.secret))
                    .withAudience(JwtConfig.audience)
                    .withIssuer(JwtConfig.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(JwtConfig.audience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ApiResponse(
                    success = false,
                    message = "Token is not valid or has expired"
                ))
            }
        }
    }
}

fun Application.configureHTTP(dotenv: io.github.cdimascio.dotenv.Dotenv) {
    install(CORS) {
        val origins = dotenv["CORS_ORIGINS"]?.split(",") ?: listOf("http://localhost:3000")
        origins.forEach { allowHost(it.removePrefix("http://").removePrefix("https://"), schemes = listOf("http", "https")) }

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true
    }
}

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}

fun Application.configureRouting(
    authController: AuthController,
    rideController: RideController,
    bookingController: BookingController,
    paymentController: PaymentController,
    ratingController: RatingController
) {
    routing {
        // Health check
        get("/health") {
            call.respond(mapOf(
                "status" to "OK",
                "timestamp" to System.currentTimeMillis(),
                "version" to "1.0.0"
            ))
        }

        // API routes
        route("/api/v1") {
            authRoutes(authController)
            rideRoutes(rideController)
            bookingRoutes(bookingController)
            paymentRoutes(paymentController)
            ratingRoutes(ratingController)
        }
    }
}

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            val isDevelopment = try {
                call.application.environment.config.property("ktor.deployment.environment").getString() == "development"
            } catch (_: Exception) {
                true
            }
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse(
                    success = false,
                    message = if (isDevelopment) {
                        cause.message ?: "Internal server error"
                    } else {
                        "Internal server error"
                    }
                )
            )
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ApiResponse(
                    success = false,
                    message = "Route not found"
                )
            )
        }
    }
}
