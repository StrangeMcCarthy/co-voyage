package covoyage.travel.cameroon.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: String = "",
    val journeyId: String = "",
    val passengerId: String = "",
    val passengerName: String = "",
    val passengerPhone: String = "",
    val seatsBooked: Int = 1,
    val totalAmount: Int = 0,         // in XAF
    val currency: String = "XAF",
    val status: BookingStatus = BookingStatus.PENDING,
    val createdAt: String = "",
)

@Serializable
enum class BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    REFUNDED
}

@Serializable
data class Payment(
    val id: String = "",
    val bookingId: String = "",
    val passengerId: String = "",
    val driverId: String = "",
    val amount: Int = 0,              // Total amount in XAF
    val platformFee: Int = 0,         // 10% kept by platform
    val driverPayout: Int = 0,        // 90% released to driver
    val currency: String = "XAF",
    val paymentMethod: PaymentMethod = PaymentMethod.MTN_MOMO,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val txRef: String = "",           // Transaction reference
    val flwRef: String = "",          // Flutterwave reference
    val createdAt: String = "",
    val releasedAt: String = "",      // When 90% was released to driver
)

@Serializable
enum class PaymentStatus {
    PENDING,
    HELD,        // Money collected and held in escrow
    RELEASED,    // 90% released to driver
    REFUNDED,    // Full refund to passenger
    FAILED
}

@Serializable
enum class PaymentMethod {
    MTN_MOMO,
    ORANGE_MONEY,
    CARD,
    CASH          // Fallback
}
