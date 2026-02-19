package covoyage.server.routes

import covoyage.server.service.NotificationService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * REST endpoints for push notification management.
 */
fun Route.notificationRoutes(notificationService: NotificationService) {

    route("/notifications") {

        /** Register or update FCM device token */
        post("/register") {
            val body = call.receive<Map<String, String>>()
            val userId = body["userId"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing userId"))
            val token = body["token"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing token"))

            notificationService.registerToken(userId, token)
            call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Token registered"))
        }

        /** Send a test notification (for debugging) */
        post("/test") {
            val body = call.receive<Map<String, String>>()
            val userId = body["userId"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing userId"))

            notificationService.sendToUser(
                userId,
                "Test Notification 🔔",
                "If you see this, push notifications are working!",
            )
            call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Test notification sent"))
        }
    }
}
