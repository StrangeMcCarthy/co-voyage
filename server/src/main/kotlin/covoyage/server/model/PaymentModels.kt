package covoyage.server.model

import kotlinx.serialization.Serializable

// ──────────────────────────────────────────────
// Server-side payment & booking models
// ──────────────────────────────────────────────

@Serializable
data class PaymentDocument(
    val id: String = "",
    val bookingId: String = "",
    val journeyId: String = "",
    val passengerId: String = "",
    val passengerName: String = "",
    val passengerEmail: String = "",
    val passengerPhone: String = "",
    val driverId: String = "",
    val amount: Int = 0,
    val platformFee: Int = 0,       // 10%
    val driverPayout: Int = 0,      // 90%
    val currency: String = "XAF",
    val paymentMethod: String = "", // "MTN_MOMO", "ORANGE_MONEY", "CARD"
    val status: String = "PENDING", // PENDING, HELD, RELEASED, REFUNDED, FAILED
    val txRef: String = "",         // Our unique reference
    val flwRef: String = "",        // Flutterwave reference
    val flwTransactionId: Long = 0, // Flutterwave transaction ID
    val createdAt: String = "",
    val updatedAt: String = "",
    val releasedAt: String = "",
)

// ──────────────────────────────────────────────
// Client→Server request/response DTOs
// ──────────────────────────────────────────────

@Serializable
data class InitiatePaymentRequest(
    val bookingId: String,
    val journeyId: String,
    val passengerId: String,
    val passengerName: String,
    val passengerEmail: String,
    val passengerPhone: String,
    val driverId: String,
    val seatsBooked: Int,
    val totalAmount: Int,
    val paymentMethod: String, // "MTN_MOMO", "ORANGE_MONEY", "CARD"
    // Card-specific (only if paymentMethod == "CARD")
    val cardNumber: String = "",
    val cvv: String = "",
    val expiryMonth: String = "",
    val expiryYear: String = "",
)

@Serializable
data class PaymentStatusResponse(
    val paymentId: String,
    val status: String,
    val txRef: String = "",
    val flwRef: String = "",
    val amount: Int = 0,
    val message: String = "",
)

@Serializable
data class ReleasePaymentRequest(
    val paymentId: String,
)

@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: PaymentStatusResponse? = null,
)
