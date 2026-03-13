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
    val imageUrl: String? = null,
    val timestamp: String = "",
    val isRead: Boolean = false,
)

/**
 * Inbound WebSocket message from client.
 */
@Serializable
data class ChatInput(
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val imageUrl: String? = null,
)

/**
 * Quick reply suggestions.
 */
@Serializable
data class QuickReply(
    val id: String,
    val text: String,
)
