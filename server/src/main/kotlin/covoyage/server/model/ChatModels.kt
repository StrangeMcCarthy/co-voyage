package covoyage.server.model

import kotlinx.serialization.Serializable

/**
 * Chat message model for booking-scoped messaging.
 */
@Serializable
data class ChatMessage(
    val id: String = "",
    val bookingId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: String = "",
)

/**
 * Inbound WebSocket message from client.
 */
@Serializable
data class ChatInput(
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
)
