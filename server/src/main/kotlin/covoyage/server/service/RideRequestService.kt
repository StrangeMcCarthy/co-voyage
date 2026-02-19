package covoyage.server.service

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import covoyage.server.database.MongoConfig
import covoyage.server.model.ApiResponse
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import org.bson.Document

/**
 * CRUD for passenger ride requests.
 */
class RideRequestService(
    private val mongoConfig: MongoConfig,
) {
    private val requests get() = mongoConfig.database.getCollection<Document>("ride_requests")

    /**
     * Create a new ride request (passenger posts a request).
     */
    suspend fun createRideRequest(doc: Document): ApiResponse {
        val id = "request-${Clock.System.now().toEpochMilliseconds()}"
        doc.put("id", id)
        doc.put("status", "OPEN")
        doc.put("createdAt", Clock.System.now().toString())
        requests.insertOne(doc)
        return ApiResponse(
            success = true,
            message = "Ride request posted",
        )
    }

    /**
     * Get all open ride requests (public feed for drivers).
     */
    suspend fun getAllOpenRequests(): List<Document> {
        return requests.find(Filters.eq("status", "OPEN")).toList()
    }

    /**
     * Get ride requests by a specific passenger.
     */
    suspend fun getRequestsByPassenger(passengerId: String): List<Document> {
        return requests.find(Filters.eq("passengerId", passengerId)).toList()
    }

    /**
     * Close a ride request. Only the owning passenger can close it.
     */
    suspend fun closeRequest(requestId: String, passengerId: String): ApiResponse {
        val request = requests.find(Filters.eq("id", requestId)).firstOrNull()
            ?: return ApiResponse(false, "Ride request not found")

        if (request.getString("passengerId") != passengerId) {
            return ApiResponse(false, "Unauthorized — not your request")
        }
        if (request.getString("status") != "OPEN") {
            return ApiResponse(false, "Request is already closed")
        }

        requests.updateOne(
            Filters.eq("id", requestId),
            Updates.combine(
                Updates.set("status", "CLOSED"),
                Updates.set("closedAt", Clock.System.now().toString()),
            ),
        )
        return ApiResponse(true, "Ride request closed")
    }
}
