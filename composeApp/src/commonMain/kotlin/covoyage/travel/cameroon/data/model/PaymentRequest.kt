package covoyage.travel.cameroon.data.model

import kotlinx.serialization.Serializable

/**
 * DTOs for client → server payment communication.
 * These mirror the server's PaymentModels.
 */

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
    val cardNumber: String = "",
    val cvv: String = "",
    val expiryMonth: String = "",
    val expiryYear: String = "",
)

@Serializable
data class PaymentStatusResponse(
    val paymentId: String = "",
    val status: String = "",
    val txRef: String = "",
    val flwRef: String = "",
    val amount: Int = 0,
    val message: String = "",
)

@Serializable
data class ApiResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: PaymentStatusResponse? = null,
)
