package com.covoyage.backend.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
enum class BookingStatus {
    PENDING_PAYMENT,
    PAYMENT_CONFIRMED,
    RIDE_STARTED,
    COMPLETED,
    CANCELLED,
    REFUNDED
}

@Serializable
data class ParcelDetails(
    val description: String,
    val weight: Double,
    val receiverName: String,
    val receiverPhone: String,
    val receiverAddress: String? = null,
    val parcelFee: Double,
    val imageUrl: String? = null
)

@Serializable
data class Booking(
    @BsonId
    val id: String = ObjectId().toHexString(),
    val rideId: String,
    val passengerId: String,
    val driverId: String,
    
    val passengerName: String,
    val passengerPhone: String,
    val passengerEmail: String,
    
    val numberOfSeats: Int,
    val totalAmount: Double,
    val platformFee: Double,
    val driverAmount: Double,
    
    val status: BookingStatus = BookingStatus.PENDING_PAYMENT,
    val paymentId: String? = null,
    
    val pickupLocation: Location,
    val dropoffLocation: Location? = null,
    
    val hasParcel: Boolean = false,
    val parcelDetails: ParcelDetails? = null,
    
    val journeyStartedAt: Long? = null,
    val journeyCompletedAt: Long? = null,
    val arrivedConfirmedAt: Long? = null,
    val paidAt: Long? = null,
    val paymentReleasedAt: Long? = null,
    
    val cancelledAt: Long? = null,
    val cancellationReason: String? = null,
    val cancelledBy: String? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class CreateBookingRequest(
    val rideId: String,
    val numberOfSeats: Int,
    val pickupLocation: Location,
    val dropoffLocation: Location? = null,
    val hasParcel: Boolean = false,
    val parcelDetails: ParcelDetails? = null
)

@Serializable
data class BookingConfirmation(
    val booking: Booking,
    val paymentUrl: String? = null,
    val qrCode: String? = null
)
