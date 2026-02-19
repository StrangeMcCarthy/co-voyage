package covoyage.server.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import covoyage.server.database.MongoConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.bson.Document
import org.slf4j.LoggerFactory

/**
 * Push notification service using Firebase Cloud Messaging (FCM).
 * Manages device token registration and sends notifications.
 */
class NotificationService(
    private val mongoConfig: MongoConfig,
) {
    private val logger = LoggerFactory.getLogger("NotificationService")
    private val tokens get() = mongoConfig.database.getCollection<Document>("fcm_tokens")
    private var firebaseInitialized = false

    init {
        try {
            // Initialize Firebase from GOOGLE_APPLICATION_CREDENTIALS env variable
            // or from FIREBASE_SERVICE_ACCOUNT_JSON env variable
            val serviceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
            if (serviceAccountJson != null) {
                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountJson.byteInputStream()))
                    .build()
                FirebaseApp.initializeApp(options)
                firebaseInitialized = true
                logger.info("Firebase initialized from FIREBASE_SERVICE_ACCOUNT_JSON")
            } else if (System.getenv("GOOGLE_APPLICATION_CREDENTIALS") != null) {
                FirebaseApp.initializeApp()
                firebaseInitialized = true
                logger.info("Firebase initialized from GOOGLE_APPLICATION_CREDENTIALS")
            } else {
                logger.warn("Firebase not configured — push notifications disabled. Set FIREBASE_SERVICE_ACCOUNT_JSON or GOOGLE_APPLICATION_CREDENTIALS")
            }
        } catch (e: Exception) {
            logger.error("Failed to initialize Firebase: ${e.message}")
        }
    }

    /**
     * Register or update a device's FCM token for a user.
     */
    suspend fun registerToken(userId: String, token: String) {
        val existing = tokens.find(Filters.eq("userId", userId)).firstOrNull()
        if (existing != null) {
            tokens.updateOne(
                Filters.eq("userId", userId),
                Updates.combine(
                    Updates.set("token", token),
                    Updates.set("updatedAt", System.currentTimeMillis()),
                ),
            )
        } else {
            tokens.insertOne(
                Document().apply {
                    put("userId", userId)
                    put("token", token)
                    put("createdAt", System.currentTimeMillis())
                    put("updatedAt", System.currentTimeMillis())
                },
            )
        }
    }

    /**
     * Send a push notification to a specific user.
     */
    suspend fun sendToUser(userId: String, title: String, body: String, data: Map<String, String> = emptyMap()) {
        if (!firebaseInitialized) {
            logger.debug("FCM not initialized, skipping notification to $userId: $title")
            return
        }

        val tokenDoc = tokens.find(Filters.eq("userId", userId)).firstOrNull()
        val fcmToken = tokenDoc?.getString("token")
        if (fcmToken == null) {
            logger.debug("No FCM token for user $userId, skipping")
            return
        }

        try {
            withContext(Dispatchers.IO) {
                val message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(
                        Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build(),
                    )
                    .putAllData(data)
                    .build()

                FirebaseMessaging.getInstance().send(message)
                logger.info("Push sent to $userId: $title")
            }
        } catch (e: Exception) {
            logger.error("Failed to send push to $userId: ${e.message}")
        }
    }

    // ── Convenience methods for specific events ──

    suspend fun notifyPaymentConfirmed(passengerId: String, amount: Int) {
        sendToUser(
            passengerId,
            "Payment Confirmed ✅",
            "Your payment of $amount XAF has been received and held in escrow.",
            mapOf("type" to "payment_confirmed", "amount" to amount.toString()),
        )
    }

    suspend fun notifyTripStarting(passengerId: String, route: String) {
        sendToUser(
            passengerId,
            "Trip Starting! 🚐",
            "Your ride on $route is now underway.",
            mapOf("type" to "trip_started", "route" to route),
        )
    }

    suspend fun notifyTripCompleted(passengerId: String, route: String) {
        sendToUser(
            passengerId,
            "Trip Completed 🎉",
            "Your ride on $route has been completed. Rate your driver!",
            mapOf("type" to "trip_completed", "route" to route),
        )
    }

    suspend fun notifyNewBooking(driverId: String, passengerName: String, route: String) {
        sendToUser(
            driverId,
            "New Booking! 📋",
            "$passengerName booked a seat on $route",
            mapOf("type" to "new_booking", "passengerName" to passengerName),
        )
    }

    suspend fun notifyPayoutReleased(driverId: String, amount: Int) {
        sendToUser(
            driverId,
            "Payout Released 💰",
            "$amount XAF has been released to your account.",
            mapOf("type" to "payout_released", "amount" to amount.toString()),
        )
    }

    suspend fun notifyChatMessage(recipientId: String, senderName: String) {
        sendToUser(
            recipientId,
            "New Message 💬",
            "$senderName sent you a message",
            mapOf("type" to "chat_message", "senderName" to senderName),
        )
    }
}
