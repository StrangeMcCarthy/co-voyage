package covoyage.server

import covoyage.server.config.FlutterwaveConfig
import covoyage.server.database.MongoConfig
import covoyage.server.routes.adminRoutes
import covoyage.server.routes.authRoutes
import covoyage.server.routes.chatRoutes
import covoyage.server.routes.dashboardRoutes
import covoyage.server.routes.journeyRoutes
import covoyage.server.routes.notificationRoutes
import covoyage.server.routes.paymentRoutes
import covoyage.server.routes.rideRequestRoutes
import covoyage.server.routes.matchingRoutes
import covoyage.server.service.AuthService
import covoyage.server.service.ChatService
import covoyage.server.service.DashboardService
import covoyage.server.service.FlutterwaveService
import covoyage.server.service.JourneyService
import covoyage.server.service.MatchingService
import covoyage.server.service.NotificationService
import covoyage.server.service.PaymentService
import covoyage.server.service.RideRequestService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.server.sessions.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import covoyage.server.security.AdminSession
import covoyage.server.security.JwtConfig
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureServer()
    }.start(wait = true)
}

fun Application.configureServer() {
    val flutterwaveConfig = FlutterwaveConfig.fromEnvironment()
    val mongoConfig = MongoConfig()
    val flutterwaveService = FlutterwaveService(flutterwaveConfig)
    val paymentService = PaymentService(mongoConfig, flutterwaveService)
    val notificationService = NotificationService(mongoConfig)
    val matchingService = MatchingService(mongoConfig, notificationService)
    val journeyService = JourneyService(mongoConfig, paymentService, notificationService, matchingService)
    val rideRequestService = RideRequestService(mongoConfig, matchingService)
    val authService = AuthService(mongoConfig)
    val chatService = ChatService(mongoConfig, notificationService)
    val dashboardService = DashboardService(mongoConfig)

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(CORS) {
        // In production, set CORS_ALLOWED_ORIGINS env variable (comma-separated)
        val allowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS")
            ?.split(",")
            ?.map { it.trim() }
            ?: listOf("http://localhost:8080", "http://10.0.2.2:8080")
        allowedOrigins.forEach { allowHost(it.removePrefix("http://").removePrefix("https://"), schemes = listOf("http", "https")) }
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }

    install(CallLogging)

    install(Sessions) {
        cookie<AdminSession>("ADMIN_SESSION") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 3600 // 1 hour
            // In production, set secure = true
            cookie.httpOnly = true
        }
    }

    install(Authentication) {
        session<AdminSession>("admin-auth") {
            validate { session -> session }
            challenge {
                call.respondRedirect("/admin/login")
            }
        }
        jwt("jwt-auth") {
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token is invalid or expired"))
            }
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Internal server error"))
            )
        }
    }

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok", "service" to "covoyage-server"))
        }

        // Serve dashboard static files
        staticResources("/dashboard", "dashboard")

        adminRoutes(dashboardService, authService)

        route("/api") {
            // Public routes — no JWT required
            authRoutes(authService, notificationService)
            paymentRoutes(paymentService) // webhook needs to be public; initiate is also public for now

            // Protected routes — require valid JWT
            authenticate("jwt-auth") {
                journeyRoutes(journeyService)
                rideRequestRoutes(rideRequestService)
                notificationRoutes(notificationService)
                chatRoutes(chatService)
                matchingRoutes(matchingService)
                dashboardRoutes(dashboardService)
            }
        }
    }
}
