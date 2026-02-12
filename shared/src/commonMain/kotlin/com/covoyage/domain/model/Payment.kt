package com.covoyage.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    HELD_IN_ESCROW,
    RELEASED_TO_DRIVER
}

@Serializable
enum class PaymentMethod {
    ORANGE_MONEY,
    MTN_MOBILE_MONEY,
    CARD,
    BANK_TRANSFER
}

@Serializable
data class Payment(
    val id: String,
    val bookingId: String,
    val userId: String,
    val amount: Double,
    val currency: String = "XAF", // Central African Franc
    val method: PaymentMethod,
    val status: PaymentStatus,
    
    // Flutterwave details
    val flutterwaveTransactionId: String? = null,
    val flutterwaveReference: String? = null,
    
    // Escrow details
    val platformFee: Double,
    val driverAmount: Double,
    val isInEscrow: Boolean = false,
    val escrowReleasedAt: Long? = null,
    
    val createdAt: Long,
    val updatedAt: Long,
    
    // Metadata
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class InitiatePaymentRequest(
    val bookingId: String,
    val amount: Double,
    val method: PaymentMethod,
    val redirectUrl: String? = null
)

@Serializable
data class InitiatePaymentResponse(
    val paymentId: String,
    val paymentUrl: String,
    val reference: String
)

@Serializable
data class PaymentWebhook(
    val event: String,
    val data: PaymentWebhookData
)

@Serializable
data class PaymentWebhookData(
    val id: String,
    val txRef: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val customer: CustomerData,
    val createdAt: String
)

@Serializable
data class CustomerData(
    val email: String,
    val phoneNumber: String,
    val name: String
)

@Serializable
data class RefundRequest(
    val paymentId: String,
    val reason: String,
    val amount: Double? = null // null for full refund
)

@Serializable
data class EscrowTransaction(
    val id: String,
    val paymentId: String,
    val bookingId: String,
    val driverId: String,
    val amount: Double,
    val platformFee: Double,
    val driverAmount: Double,
    val status: PaymentStatus,
    val heldAt: Long,
    val scheduledReleaseAt: Long, // Auto-release after 24h
    val actualReleaseAt: Long? = null,
    val releaseReason: String? = null
)
