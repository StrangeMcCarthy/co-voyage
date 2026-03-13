package covoyage.server.model

import kotlinx.serialization.Serializable

// ──────────────────────────────────────────────
// Dashboard DTOs (moved from DashboardService)
// ──────────────────────────────────────────────

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

@Serializable
data class UserRow(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val status: String,
    val createdAt: String,
)
