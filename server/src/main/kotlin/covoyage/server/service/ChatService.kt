package covoyage.server.service

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import covoyage.server.database.MongoConfig
import covoyage.server.model.ChatMessage
import io.ktor.websocket.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import org.bson.Document
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Chat service managing WebSocket sessions and message persistence.
 * Messages are scoped to bookings (one chat room per booking).
 */
class ChatService(
    private val mongoConfig: MongoConfig,
    private val notificationService: NotificationService,
) {
    private val logger = LoggerFactory.getLogger("ChatService")
    private val messages get() = mongoConfig.database.getCollection<Document>("chat_messages")

    /**
     * Active WebSocket sessions per booking.
     * Key: bookingId, Value: Set of (userId, session) pairs.
     */
    private val sessions = ConcurrentHashMap<String, MutableSet<ChatSession>>()

    data class ChatSession(
        val userId: String,
        val session: WebSocketSession,
    )

    /**
     * Register a WebSocket session for a booking.
     */
    fun addSession(bookingId: String, userId: String, session: WebSocketSession) {
        sessions.getOrPut(bookingId) { ConcurrentHashMap.newKeySet() }.add(ChatSession(userId, session))
        logger.info("Chat session added: booking=$bookingId, user=$userId")
    }

    /**
     * Remove a WebSocket session.
     */
    fun removeSession(bookingId: String, userId: String, session: WebSocketSession) {
        sessions[bookingId]?.remove(ChatSession(userId, session))
        if (sessions[bookingId]?.isEmpty() == true) {
            sessions.remove(bookingId)
        }
        logger.info("Chat session removed: booking=$bookingId, user=$userId")
    }

    /**
     * Save a message and broadcast to all connected participants.
     */
    suspend fun sendMessage(bookingId: String, senderId: String, senderName: String, text: String, imageUrl: String? = null): ChatMessage {
        val msgId = "msg-${UUID.randomUUID()}"
        val timestamp = Clock.System.now().toString()

        val chatMessage = ChatMessage(
            id = msgId,
            bookingId = bookingId,
            senderId = senderId,
            senderName = senderName,
            text = text,
            imageUrl = imageUrl,
            timestamp = timestamp,
            isRead = false,
        )

        // Persist to MongoDB
        messages.insertOne(
            Document().apply {
                put("id", msgId)
                put("bookingId", bookingId)
                put("senderId", senderId)
                put("senderName", senderName)
                put("text", text)
                put("imageUrl", imageUrl)
                put("timestamp", timestamp)
                put("isRead", false)
            },
        )

        // Broadcast to all connected sessions for this booking
        val json = kotlinx.serialization.json.Json.encodeToString(
            ChatMessage.serializer(), chatMessage,
        )
        sessions[bookingId]?.forEach { chatSession ->
            if (chatSession.userId != senderId) {
                try {
                    chatSession.session.send(Frame.Text(json))
                } catch (e: Exception) {
                    logger.warn("Failed to send to ${chatSession.userId}: ${e.message}")
                }
            }
        }

        // Send push notification to offline participants
        sessions[bookingId]?.let { activeSessions ->
            val activeUserIds = activeSessions.map { it.userId }.toSet()
            // If recipient is not in active sessions, send push
            // We need to find the other participant
            val chatParticipants = getParticipantIds(bookingId)
            chatParticipants.forEach { participantId ->
                if (participantId != senderId && participantId !in activeUserIds) {
                    notificationService.notifyChatMessage(participantId, senderName)
                }
            }
        }

        return chatMessage
    }

    /**
     * Get chat history for a booking.
     */
    suspend fun getMessages(bookingId: String): List<ChatMessage> {
        return messages.find(Filters.eq("bookingId", bookingId))
            .sort(Sorts.ascending("timestamp"))
            .toList()
            .map { doc ->
                ChatMessage(
                    id = doc.getString("id") ?: "",
                    bookingId = doc.getString("bookingId") ?: "",
                    senderId = doc.getString("senderId") ?: "",
                    senderName = doc.getString("senderName") ?: "",
                    text = doc.getString("text") ?: "",
                    imageUrl = doc.getString("imageUrl"),
                    timestamp = doc.getString("timestamp") ?: "",
                    isRead = doc.getBoolean("isRead") ?: false,
                )
            }
    }

    /**
     * Mark all messages in a booking as read for a specific recipient.
     */
    suspend fun markMessagesAsRead(bookingId: String, recipientId: String) {
        messages.updateMany(
            Filters.and(
                Filters.eq("bookingId", bookingId),
                Filters.ne("senderId", recipientId),
                Filters.eq("isRead", false)
            ),
            com.mongodb.client.model.Updates.set("isRead", true)
        )
        logger.info("Messages marked as read for booking $bookingId, user $recipientId")
    }

    /**
     * Get quick reply suggestions.
     */
    fun getQuickReplies(): List<covoyage.server.model.QuickReply> {
        return listOf(
            covoyage.server.model.QuickReply("1", "I'm on my way! 🚗"),
            covoyage.server.model.QuickReply("2", "I've arrived at the pickup point. 📍"),
            covoyage.server.model.QuickReply("3", "Could you please confirm your location? ❓"),
            covoyage.server.model.QuickReply("4", "I'm running a few minutes late. ⏳"),
            covoyage.server.model.QuickReply("5", "Okay, thanks! 👍")
        )
    }

    /**
     * Get participant IDs for a booking from bookings AND existing messages.
     * This ensures newly booked passengers (who haven't sent messages yet) still get notifications.
     */
    private suspend fun getParticipantIds(bookingId: String): Set<String> {
        val fromMessages = messages.find(Filters.eq("bookingId", bookingId))
            .toList()
            .map { it.getString("senderId") ?: "" }
            .filter { it.isNotBlank() }
            .toSet()

        // Also check the bookings collection for the passenger and driver
        val bookings = mongoConfig.database.getCollection<Document>("bookings")
        val booking = bookings.find(Filters.eq("id", bookingId)).firstOrNull()
        val fromBooking = listOfNotNull(
            booking?.getString("passengerId"),
            booking?.getString("driverId"),
        ).filter { it.isNotBlank() }.toSet()

        return fromMessages + fromBooking
    }
}
