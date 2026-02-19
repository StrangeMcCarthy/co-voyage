package covoyage.travel.cameroon.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String = "",
    val bookingId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: String = "",
)

@Serializable
data class ChatInput(
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
)
