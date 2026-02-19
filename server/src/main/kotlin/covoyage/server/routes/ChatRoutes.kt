package covoyage.server.routes

import covoyage.server.model.ChatInput
import covoyage.server.service.ChatService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json

/**
 * Chat routes: REST for history, WebSocket for real-time messaging.
 */
fun Route.chatRoutes(chatService: ChatService) {

    route("/chat") {

        /** Get chat history for a booking */
        get("/{bookingId}") {
            val bookingId = call.parameters["bookingId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing bookingId")
            val messages = chatService.getMessages(bookingId)
            call.respond(HttpStatusCode.OK, messages)
        }

        /** Send a message via REST (alternative to WebSocket) */
        post("/{bookingId}/send") {
            val bookingId = call.parameters["bookingId"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing bookingId")
            val input = call.receive<ChatInput>()
            val message = chatService.sendMessage(
                bookingId = bookingId,
                senderId = input.senderId,
                senderName = input.senderName,
                text = input.text,
            )
            call.respond(HttpStatusCode.OK, message)
        }

        /** WebSocket endpoint for real-time chat */
        webSocket("/{bookingId}/ws") {
            val bookingId = call.parameters["bookingId"] ?: return@webSocket close(
                CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing bookingId")
            )
            val userId = call.request.queryParameters["userId"] ?: return@webSocket close(
                CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing userId")
            )

            chatService.addSession(bookingId, userId, this)

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        try {
                            val input = Json.decodeFromString<ChatInput>(text)
                            val message = chatService.sendMessage(
                                bookingId = bookingId,
                                senderId = input.senderId.ifBlank { userId },
                                senderName = input.senderName,
                                text = input.text,
                            )
                            // Echo back to sender as confirmation
                            send(Frame.Text(Json.encodeToString(
                                covoyage.server.model.ChatMessage.serializer(), message,
                            )))
                        } catch (e: Exception) {
                            send(Frame.Text("""{"error":"${e.message}"}"""))
                        }
                    }
                }
            } finally {
                chatService.removeSession(bookingId, userId, this)
            }
        }
    }
}
