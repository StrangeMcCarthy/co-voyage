package covoyage.server.service

import com.mongodb.client.model.Filters
import covoyage.server.database.MongoConfig
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import org.bson.Document

/**
 * Dashboard analytics service.
 * Aggregates data from MongoDB for the admin dashboard.
 */
class DashboardService(private val mongoConfig: MongoConfig) {

    private val payments get() = mongoConfig.database.getCollection<Document>("payments")
    private val journeys get() = mongoConfig.database.getCollection<Document>("journeys")
    private val bookings get() = mongoConfig.database.getCollection<Document>("bookings")
    private val users get() = mongoConfig.database.getCollection<Document>("users")

    /**
     * Overview stats for dashboard header cards.
     */
    suspend fun getOverviewStats(): DashboardStats {
        val allPayments = payments.find().toList()
        val allJourneys = journeys.find().toList()
        val allBookings = bookings.find().toList()
        val allUsers = users.find().toList()

        val totalRevenue = allPayments
            .filter { it.getString("status") in listOf("HELD", "RELEASED") }
            .sumOf { it.getInteger("amount", 0) }

        val platformFees = allPayments
            .filter { it.getString("status") in listOf("HELD", "RELEASED") }
            .sumOf { it.getInteger("platformFee", 0) }

        val pendingPayments = allPayments.count { it.getString("status") == "PENDING" }
        val heldPayments = allPayments.count { it.getString("status") == "HELD" }
        val releasedPayments = allPayments.count { it.getString("status") == "RELEASED" }
        val failedPayments = allPayments.count { it.getString("status") == "FAILED" }

        return DashboardStats(
            totalRevenue = totalRevenue,
            platformFees = platformFees,
            totalPayments = allPayments.size,
            pendingPayments = pendingPayments,
            heldPayments = heldPayments,
            releasedPayments = releasedPayments,
            failedPayments = failedPayments,
            totalJourneys = allJourneys.size,
            totalBookings = allBookings.size,
            totalUsers = allUsers.size,
        )
    }

    /**
     * Recent payments for the transactions table.
     */
    suspend fun getRecentPayments(limit: Int = 50): List<PaymentRow> {
        return payments.find().toList()
            .sortedByDescending { it.getString("createdAt") ?: "" }
            .take(limit)
            .map { doc ->
                PaymentRow(
                    id = doc.getString("id") ?: "",
                    passengerName = doc.getString("passengerName") ?: "",
                    passengerPhone = doc.getString("passengerPhone") ?: "",
                    amount = doc.getInteger("amount", 0),
                    platformFee = doc.getInteger("platformFee", 0),
                    driverPayout = doc.getInteger("driverPayout", 0),
                    paymentMethod = doc.getString("paymentMethod") ?: "",
                    status = doc.getString("status") ?: "",
                    txRef = doc.getString("txRef") ?: "",
                    flwRef = doc.getString("flwRef") ?: "",
                    createdAt = doc.getString("createdAt") ?: "",
                )
            }
    }

    /**
     * Revenue breakdown by payment method.
     */
    suspend fun getRevenueByMethod(): List<RevenueByMethod> {
        val allPayments = payments.find().toList()
            .filter { it.getString("status") in listOf("HELD", "RELEASED") }

        return listOf("MTN_MOMO", "ORANGE_MONEY", "CARD").map { method ->
            val methodPayments = allPayments.filter { it.getString("paymentMethod") == method }
            RevenueByMethod(
                method = method,
                count = methodPayments.size,
                totalAmount = methodPayments.sumOf { it.getInteger("amount", 0) },
                platformFees = methodPayments.sumOf { it.getInteger("platformFee", 0) },
            )
        }
    }

    /**
     * All journeys for the journeys table.
     */
    suspend fun getAllJourneys(): List<JourneyRow> {
        return journeys.find().toList().map { doc ->
            JourneyRow(
                id = doc.getString("id") ?: "",
                departureCity = doc.getString("departureCity") ?: "",
                arrivalCity = doc.getString("arrivalCity") ?: "",
                driverName = doc.getString("driverName") ?: "",
                departureDate = doc.getString("departureDate") ?: "",
                totalSeats = doc.getInteger("totalSeats", 0),
                availableSeats = doc.getInteger("availableSeats", 0),
                pricePerSeat = doc.getInteger("pricePerSeat", 0),
                status = doc.getString("status") ?: "",
            )
        }
    }

    /**
     * All bookings for the bookings table.
     */
    suspend fun getAllBookings(): List<BookingRow> {
        return bookings.find().toList().map { doc ->
            BookingRow(
                id = doc.getString("id") ?: "",
                journeyId = doc.getString("journeyId") ?: "",
                passengerName = doc.getString("passengerName") ?: "",
                seatsBooked = doc.getInteger("seatsBooked", 0),
                totalAmount = doc.getInteger("totalAmount", 0),
                status = doc.getString("status") ?: "",
                createdAt = doc.getString("createdAt") ?: "",
            )
        }
    }
}

// ── Dashboard DTOs ──

@Serializable
data class DashboardStats(
    val totalRevenue: Int = 0,
    val platformFees: Int = 0,
    val totalPayments: Int = 0,
    val pendingPayments: Int = 0,
    val heldPayments: Int = 0,
    val releasedPayments: Int = 0,
    val failedPayments: Int = 0,
    val totalJourneys: Int = 0,
    val totalBookings: Int = 0,
    val totalUsers: Int = 0,
)

@Serializable
data class PaymentRow(
    val id: String,
    val passengerName: String,
    val passengerPhone: String,
    val amount: Int,
    val platformFee: Int,
    val driverPayout: Int,
    val paymentMethod: String,
    val status: String,
    val txRef: String,
    val flwRef: String,
    val createdAt: String,
)

@Serializable
data class RevenueByMethod(
    val method: String,
    val count: Int,
    val totalAmount: Int,
    val platformFees: Int,
)

@Serializable
data class JourneyRow(
    val id: String,
    val departureCity: String,
    val arrivalCity: String,
    val driverName: String,
    val departureDate: String,
    val totalSeats: Int,
    val availableSeats: Int,
    val pricePerSeat: Int,
    val status: String,
)

@Serializable
data class BookingRow(
    val id: String,
    val journeyId: String,
    val passengerName: String,
    val seatsBooked: Int,
    val totalAmount: Int,
    val status: String,
    val createdAt: String,
)
