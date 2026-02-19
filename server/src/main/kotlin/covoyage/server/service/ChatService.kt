package covoyage.server.service

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import covoyage.server.database.MongoConfig
import covoyage.server.model.ChatMessage
import io.ktor.websocket.*
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import org.bson.Document
import org.slf4j.LoggerFactory
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
        sessions.getOrPut(bookingId) { mutableSetOf() }.add(ChatSession(userId, session))
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
    suspend fun sendMessage(bookingId: String, senderId: String, senderName: String, text: String): ChatMessage {
        val msgId = "msg-${Clock.System.now().toEpochMilliseconds()}"
        val timestamp = Clock.System.now().toString()

        val chatMessage = ChatMessage(
            id = msgId,
            bookingId = bookingId,
            senderId = senderId,
            senderName = senderName,
            text = text,
            timestamp = timestamp,
        )

        // Persist to MongoDB
        messages.insertOne(
            Document().apply {
                put("id", msgId)
                put("bookingId", bookingId)
                put("senderId", senderId)
                put("senderName", senderName)
                put("text", text)
                put("timestamp", timestamp)
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
                    timestamp = doc.getString("timestamp") ?: "",
                )
            }
    }

    /**
     * Get participant IDs for a booking from existing messages.
     */
    private suspend fun getParticipantIds(bookingId: String): Set<String> {
        return messages.find(Filters.eq("bookingId", bookingId))
            .toList()
            .map { it.getString("senderId") ?: "" }
            .filter { it.isNotBlank() }
            .toSet()
    }
}
