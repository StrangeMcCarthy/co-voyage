package covoyage.server.service

import com.mongodb.client.model.Filters
import covoyage.server.database.MongoConfig
import covoyage.server.model.*
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.*
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
        return try {
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

            DashboardStats(
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
        } catch (e: Exception) {
            println("DashboardService: Error fetching stats (MongoDB might be down): ${e.message}")
            DashboardStats() // Return empty stats
        }
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

    /**
     * All users for the user management table.
     */
    suspend fun getAllUsers(): List<UserRow> {
        return try {
            users.find().toList().map { doc ->
                UserRow(
                    id = doc.getString("id") ?: "",
                    name = doc.getString("name") ?: "(No Name)",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    role = doc.getString("userType") ?: "PASSENGER",
                    status = doc.getString("status") ?: "Active",
                    createdAt = doc.getString("createdAt") ?: ""
                )
            }
        } catch (e: Exception) {
            println("DashboardService: Error fetching users: ${e.message}")
            emptyList()
        }
    }

    /**
     * Aggregate journey counts by day of week (Mon–Sun) for ride volume chart.
     * Parses `departureDate` strings and groups by day-of-week.
     */
    suspend fun getRideVolumeByDayOfWeek(): List<Int> {
        return try {
            val journeys = mongoConfig.database.getCollection<Document>("journeys")
            val dayCountMap = mutableMapOf<Int, Int>() // 1=Mon,...,7=Sun
            journeys.find().toList().forEach { doc ->
                val dateStr = doc.getString("departureDate") ?: return@forEach
                try {
                    val date = kotlinx.datetime.LocalDate.parse(dateStr)
                    val dow = date.dayOfWeek.value // 1=Monday..7=Sunday
                    dayCountMap[dow] = (dayCountMap[dow] ?: 0) + 1
                } catch (_: Exception) { /* skip unparseable dates */ }
            }
            // Return Mon..Sun in order
            (1..7).map { dayCountMap[it] ?: 0 }
        } catch (e: Exception) {
            println("DashboardService: Error fetching ride volume: ${e.message}")
            listOf(0, 0, 0, 0, 0, 0, 0)
        }
    }

    /**
     * Count users registered per week in the current month for user growth chart.
     */
    suspend fun getUserGrowthByWeek(): List<Int> {
        return try {
            val users = mongoConfig.database.getCollection<Document>("users")
            val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
            val now = kotlinx.datetime.Clock.System.now()
            val nowLocal = now.toLocalDateTime(tz)
            val currentYear = nowLocal.year
            val currentMonth = nowLocal.monthNumber

            val weekCounts = mutableMapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0)
            users.find().toList().forEach { doc ->
                val createdAt = doc.getString("createdAt") ?: return@forEach
                try {
                    // Parse ISO instant string
                    val instant = kotlinx.datetime.Instant.parse(createdAt)
                    val localDate = instant.toLocalDateTime(tz)
                    if (localDate.year == currentYear && localDate.monthNumber == currentMonth) {
                        val weekOfMonth = ((localDate.dayOfMonth - 1) / 7 + 1).coerceIn(1, 4)
                        weekCounts[weekOfMonth] = (weekCounts[weekOfMonth] ?: 0) + 1
                    }
                } catch (_: Exception) { /* skip unparseable dates */ }
            }
            (1..4).map { weekCounts[it] ?: 0 }
        } catch (e: Exception) {
            println("DashboardService: Error fetching user growth: ${e.message}")
            listOf(0, 0, 0, 0)
        }
    }
}

