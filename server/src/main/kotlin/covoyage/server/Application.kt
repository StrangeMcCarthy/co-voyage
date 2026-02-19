package covoyage.server

import covoyage.server.config.FlutterwaveConfig
import covoyage.server.database.MongoConfig
import covoyage.server.routes.authRoutes
import covoyage.server.routes.chatRoutes
import covoyage.server.routes.dashboardRoutes
import covoyage.server.routes.journeyRoutes
import covoyage.server.routes.notificationRoutes
import covoyage.server.routes.paymentRoutes
import covoyage.server.routes.rideRequestRoutes
import covoyage.server.service.AuthService
import covoyage.server.service.ChatService
import covoyage.server.service.DashboardService
import covoyage.server.service.FlutterwaveService
import covoyage.server.service.JourneyService
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
    val journeyService = JourneyService(mongoConfig, paymentService, notificationService)
    val rideRequestService = RideRequestService(mongoConfig)
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
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }

    install(CallLogging)

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

        route("/api") {
            authRoutes(authService)
            paymentRoutes(paymentService)
            journeyRoutes(journeyService)
            rideRequestRoutes(rideRequestService)
            notificationRoutes(notificationService)
            chatRoutes(chatService)
            dashboardRoutes(dashboardService)
        }
    }
}
