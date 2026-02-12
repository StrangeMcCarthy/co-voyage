package com.covoyage.domain.model

import kotlinx.serialization.Serializable

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
data class Booking(
    val id: String,
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
    
    val status: BookingStatus,
    val paymentId: String? = null,
    
    val pickupLocation: Location,
    val dropoffLocation: Location? = null,
    
    val hasParcel: Boolean = false,
    val parcelDetails: ParcelDetails? = null,
    
    val createdAt: Long,
    val updatedAt: Long,
    
    // Journey tracking
    val journeyStartedAt: Long? = null,
    val journeyCompletedAt: Long? = null,
    val arrivedConfirmedAt: Long? = null,
    
    // Payment tracking
    val paidAt: Long? = null,
    val paymentReleasedAt: Long? = null
)

@Serializable
data class ParcelDetails(
    val description: String,
    val weight: Double, // in kg
    val receiverName: String,
    val receiverPhone: String,
    val receiverAddress: String? = null,
    val parcelFee: Double,
    val imageUrl: String? = null
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

@Serializable
data class BookingHistory(
    val bookings: List<Booking>,
    val total: Int,
    val completed: Int,
    val cancelled: Int
)
