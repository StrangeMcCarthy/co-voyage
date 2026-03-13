package covoyage.server.service

import com.mongodb.client.model.Filters
import covoyage.server.database.MongoConfig
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import org.bson.Document
import org.slf4j.LoggerFactory

/**
 * Intelligent matching engine.
 * Handles "Follow Route" subscriptions and triggers alerts.
 */
class MatchingService(
    private val mongoConfig: MongoConfig,
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger("MatchingService")
    private val subscriptions get() = mongoConfig.database.getCollection<Document>("route_subscriptions")

    /**
     * Subscribe a user to alerts for a specific route.
     */
    suspend fun followRoute(userId: String, departureCity: String, arrivalCity: String) {
        val doc = Document().apply {
            put("userId", userId)
            put("departureCity", departureCity.trim().lowercase())
            put("arrivalCity", arrivalCity.trim().lowercase())
            put("createdAt", Clock.System.now().toString())
        }
        subscriptions.insertOne(doc)
        logger.info("User $userId is now following $departureCity -> $arrivalCity")
    }

    /**
     * Notify users who are following a route when a new journey or request is posted.
     */
    suspend fun notifyMatches(departureCity: String, arrivalCity: String, type: String, id: String) {
        val dep = departureCity.trim().lowercase()
        val arr = arrivalCity.trim().lowercase()

        val matches = subscriptions.find(
            Filters.and(
                Filters.eq("departureCity", dep),
                Filters.eq("arrivalCity", arr)
            )
        ).toList()

        matches.forEach { sub ->
            val userId = sub.getString("userId") ?: return@forEach
            val title = if (type == "JOURNEY") "New Ride Found! 🚐" else "New Ride Request! 📋"
            val body = if (type == "JOURNEY") {
                "A new ride from $departureCity to $arrivalCity is available."
            } else {
                "A passenger is looking for a ride from $departureCity to $arrivalCity."
            }

            notificationService.sendToUser(userId, title, body, mapOf(
                "type" to "matching_alert",
                "matchType" to type,
                "matchId" to id,
                "departure" to departureCity,
                "arrival" to arrivalCity
            ))
            
            // Also send SMS for offline users (Mock)
            // In a real app, we'd check user preferences first
            notificationService.sendSms("+237...", "CoVoyage match: $body Check the app for details!")
        }
    }
}
