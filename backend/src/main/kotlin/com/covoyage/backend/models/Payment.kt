package com.covoyage.backend.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

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
    @BsonId
    val id: String = ObjectId().toHexString(),
    val bookingId: String,
    val userId: String,
    val amount: Double,
    val currency: String = "XAF",
    val method: PaymentMethod,
    val status: PaymentStatus = PaymentStatus.PENDING,
    
    val flutterwaveTransactionId: String? = null,
    val flutterwaveReference: String? = null,
    
    val platformFee: Double,
    val driverAmount: Double,
    val isInEscrow: Boolean = false,
    val escrowReleasedAt: Long? = null,
    val scheduledReleaseAt: Long? = null,
    
    val metadata: Map<String, String> = emptyMap(),
    
    val errorMessage: String? = null,
    val errorCode: String? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
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
data class Rating(
    @BsonId
    val id: String = ObjectId().toHexString(),
    val bookingId: String,
    val reviewerId: String,
    val reviewedUserId: String,
    val rating: Int,
    val comment: String? = null,
    val punctuality: Int? = null,
    val cleanliness: Int? = null,
    val communication: Int? = null,
    val safety: Int? = null,
    val comfort: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class CreateRatingRequest(
    val bookingId: String,
    val reviewedUserId: String,
    val rating: Int,
    val comment: String? = null,
    val punctuality: Int? = null,
    val cleanliness: Int? = null,
    val communication: Int? = null,
    val safety: Int? = null,
    val comfort: Int? = null
)
