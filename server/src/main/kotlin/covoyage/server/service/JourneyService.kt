package covoyage.server.service

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import covoyage.server.database.MongoConfig
import covoyage.server.model.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import org.bson.Document
import java.util.UUID

/**
 * Journey CRUD + trip lifecycle management.
 * Handles journey creation, editing, deletion, and trip start/complete flows.
 */
class JourneyService(
    private val mongoConfig: MongoConfig,
    private val paymentService: PaymentService,
    private val notificationService: NotificationService,
    private val matchingService: MatchingService,
) {
    private val journeys get() = mongoConfig.database.getCollection<Document>("journeys")
    private val payments get() = mongoConfig.database.getCollection<Document>("payments")

    /**
     * Create a new journey. Driver posts a trip.
     */
    suspend fun createJourney(doc: Document): ApiResponse {
        val id = "journey-${UUID.randomUUID()}"
        doc.put("id", id)
        doc.put("status", "SCHEDULED")
        doc.put("createdAt", Clock.System.now().toString())
        journeys.insertOne(doc)
        return ApiResponse(
            success = true,
            message = "Journey created",
            data = PaymentStatusResponse(paymentId = id, status = "SCHEDULED"),
        ).also {
            matchingService.notifyMatches(
                doc.getString("departureCity") ?: "",
                doc.getString("arrivalCity") ?: "",
                "JOURNEY",
                id
            )
        }
    }

    /**
     * Edit a journey. Only the owning driver can edit, and only if SCHEDULED.
     */
    suspend fun editJourney(journeyId: String, driverId: String, updates: Document): ApiResponse {
        val journey = journeys.find(Filters.eq("id", journeyId)).firstOrNull()
            ?: return ApiResponse(false, "Journey not found")

        if (journey.getString("driverId") != driverId) {
            return ApiResponse(false, "Unauthorized — not your journey")
        }
        if (journey.getString("status") != "SCHEDULED") {
            return ApiResponse(false, "Cannot edit a journey that is already in progress or completed")
        }

        val updateOps = mutableListOf(
            Updates.set("updatedAt", Clock.System.now().toString()),
        )
        updates.getString("departureCity")?.let { updateOps.add(Updates.set("departureCity", it)) }
        updates.getString("arrivalCity")?.let { updateOps.add(Updates.set("arrivalCity", it)) }
        updates.getString("departureDate")?.let { updateOps.add(Updates.set("departureDate", it)) }
        updates.getString("departureTime")?.let { updateOps.add(Updates.set("departureTime", it)) }
        updates.getInteger("totalSeats")?.let { newTotal ->
            val oldTotal = journey.getInteger("totalSeats", 0)
            val oldAvailable = journey.getInteger("availableSeats", 0)
            val bookedSeats = oldTotal - oldAvailable
            val newAvailable = (newTotal - bookedSeats).coerceAtLeast(0)
            updateOps.add(Updates.set("totalSeats", newTotal))
            updateOps.add(Updates.set("availableSeats", newAvailable))
        }
        updates.getInteger("pricePerSeat")?.let { updateOps.add(Updates.set("pricePerSeat", it)) }
        updates.getString("vehicleName")?.let { updateOps.add(Updates.set("vehicleName", it)) }
        updates.getString("vehicleModel")?.let { updateOps.add(Updates.set("vehicleModel", it)) }
        updates.getString("vehiclePlateNumber")?.let { updateOps.add(Updates.set("vehiclePlateNumber", it)) }
        updates.getString("additionalNotes")?.let { updateOps.add(Updates.set("additionalNotes", it)) }

        journeys.updateOne(Filters.eq("id", journeyId), Updates.combine(updateOps))
        return ApiResponse(true, "Journey updated")
    }

    /**
     * Cancel/delete a journey. Only SCHEDULED journeys can be cancelled.
     */
    suspend fun cancelJourney(journeyId: String, driverId: String): ApiResponse {
        val journey = journeys.find(Filters.eq("id", journeyId)).firstOrNull()
            ?: return ApiResponse(false, "Journey not found")

        if (journey.getString("driverId") != driverId) {
            return ApiResponse(false, "Unauthorized — not your journey")
        }
        if (journey.getString("status") !in listOf("SCHEDULED")) {
            return ApiResponse(false, "Cannot cancel a journey that is in progress or completed")
        }

        journeys.updateOne(
            Filters.eq("id", journeyId),
            Updates.combine(
                Updates.set("status", "CANCELLED"),
                Updates.set("updatedAt", Clock.System.now().toString()),
            ),
        )

        // Refund any held payments for this journey
        val heldPayments = payments.find(
            Filters.and(
                Filters.eq("journeyId", journeyId),
                Filters.eq("status", "HELD"),
            )
        ).toList()

        heldPayments.forEach { payment ->
            val paymentId = payment.getString("id") ?: return@forEach
            paymentService.refundPayment(paymentId)
        }

        return ApiResponse(true, "Journey cancelled. ${heldPayments.size} payment(s) refunded.")
    }

    /**
     * Get all journeys by a driver.
     */
    suspend fun getJourneysByDriver(driverId: String): List<Document> {
        return journeys.find(Filters.eq("driverId", driverId)).toList()
    }

    /**
     * Get all journeys (public feed).
     * Automatically filters out past trips (departureDate < today).
     */
    suspend fun getAllJourneys(): List<Document> {
        val today = Clock.System.now().toString().take(10) // "YYYY-MM-DD"
        return journeys.find(
            Filters.and(
                Filters.eq("status", "SCHEDULED"),
                Filters.gte("departureDate", today)
            )
        ).toList()
    }

    /**
     * Start a trip. Transitions SCHEDULED → IN_PROGRESS.
     */
    suspend fun startTrip(journeyId: String, driverId: String): ApiResponse {
        val journey = journeys.find(Filters.eq("id", journeyId)).firstOrNull()
            ?: return ApiResponse(false, "Journey not found")

        if (journey.getString("driverId") != driverId) {
            return ApiResponse(false, "Unauthorized — not your journey")
        }
        if (journey.getString("status") != "SCHEDULED") {
            return ApiResponse(false, "Trip can only be started when SCHEDULED")
        }

        journeys.updateOne(
            Filters.eq("id", journeyId),
            Updates.combine(
                Updates.set("status", "IN_PROGRESS"),
                Updates.set("startedAt", Clock.System.now().toString()),
            ),
        )

        // Notify passengers with held payments
        val route = "${journey.getString("departureCity")} → ${journey.getString("arrivalCity")}"
        val bookedPassengers = payments.find(
            Filters.and(
                Filters.eq("journeyId", journeyId),
                Filters.`in`("status", listOf("HELD")),
            )
        ).toList()
        bookedPassengers.forEach { payment ->
            val passengerId = payment.getString("passengerId") ?: return@forEach
            notificationService.notifyTripStarting(passengerId, route)
        }

        return ApiResponse(true, "Trip started!")
    }

    /**
     * Complete a trip. Transitions IN_PROGRESS → COMPLETED.
     * Automatically releases all HELD escrow payments for this journey.
     */
    suspend fun completeTrip(journeyId: String, driverId: String): ApiResponse {
        val journey = journeys.find(Filters.eq("id", journeyId)).firstOrNull()
            ?: return ApiResponse(false, "Journey not found")

        if (journey.getString("driverId") != driverId) {
            return ApiResponse(false, "Unauthorized — not your journey")
        }
        if (journey.getString("status") != "IN_PROGRESS") {
            return ApiResponse(false, "Trip can only be completed when IN_PROGRESS")
        }

        // Mark journey completed
        journeys.updateOne(
            Filters.eq("id", journeyId),
            Updates.combine(
                Updates.set("status", "COMPLETED"),
                Updates.set("completedAt", Clock.System.now().toString()),
            ),
        )

        // Release all held payments → driver gets 90%
        val heldPayments = payments.find(
            Filters.and(
                Filters.eq("journeyId", journeyId),
                Filters.eq("status", "HELD"),
            )
        ).toList()

        var released = 0
        var totalPayout = 0
        heldPayments.forEach { payment ->
            val paymentId = payment.getString("id") ?: return@forEach
            val result = paymentService.releasePayment(paymentId)
            if (result.success) {
                released++
                totalPayout += payment.getInteger("driverPayout", 0)
            }
        }

        return ApiResponse(
            true,
            "Trip completed! $released payment(s) released. Payout: $totalPayout XAF",
        ).also {
            // Notify passengers of completion
            val route = "${journey.getString("departureCity")} → ${journey.getString("arrivalCity")}"
            heldPayments.forEach { payment ->
                val passengerId = payment.getString("passengerId") ?: return@forEach
                notificationService.notifyTripCompleted(passengerId, route)
            }
            // Notify driver of payout
            if (totalPayout > 0) {
                notificationService.notifyPayoutReleased(driverId, totalPayout)
            }
        }
    }

    /**
     * Get driver payout history — all released payments for this driver.
     */
    suspend fun getDriverPayouts(driverId: String): DriverPayoutSummary {
        val allPayments = payments.find(Filters.eq("driverId", driverId)).toList()

        val released = allPayments.filter { it.getString("status") == "RELEASED" }
        val held = allPayments.filter { it.getString("status") == "HELD" }

        val totalEarned = released.sumOf { it.getInteger("driverPayout", 0) }
        val pendingEarnings = held.sumOf { it.getInteger("driverPayout", 0) }

        val payouts = released.map { doc ->
            PayoutRow(
                paymentId = doc.getString("id") ?: "",
                journeyId = doc.getString("journeyId") ?: "",
                passengerName = doc.getString("passengerName") ?: "",
                totalAmount = doc.getInteger("amount", 0),
                driverPayout = doc.getInteger("driverPayout", 0),
                platformFee = doc.getInteger("platformFee", 0),
                paymentMethod = doc.getString("paymentMethod") ?: "",
                releasedAt = doc.getString("releasedAt") ?: doc.getString("updatedAt") ?: "",
            )
        }

        return DriverPayoutSummary(
            totalEarned = totalEarned,
            pendingEarnings = pendingEarnings,
            totalTrips = released.size,
            payouts = payouts,
        )
    }
}

