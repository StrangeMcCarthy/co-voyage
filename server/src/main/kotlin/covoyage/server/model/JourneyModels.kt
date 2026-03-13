package covoyage.server.model

import kotlinx.serialization.Serializable

// ──────────────────────────────────────────────
// Journey-related DTOs (moved from JourneyService)
// ──────────────────────────────────────────────

@Serializable
data class DriverPayoutSummary(
    val totalEarned: Int = 0,
    val pendingEarnings: Int = 0,
    val totalTrips: Int = 0,
    val payouts: List<PayoutRow> = emptyList(),
)

@Serializable
data class PayoutRow(
    val paymentId: String,
    val journeyId: String,
    val passengerName: String,
    val totalAmount: Int,
    val driverPayout: Int,
    val platformFee: Int,
    val paymentMethod: String,
    val releasedAt: String,
)
