package com.covoyage.backend.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
enum class RideStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

@Serializable
enum class RideType {
    PASSENGER_REQUEST,
    DRIVER_OFFER
}

@Serializable
data class Location(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

@Serializable
data class Ride(
    @BsonId
    val id: String = ObjectId().toHexString(),
    val driverId: String,
    val driverName: String,
    val driverPhone: String,
    val driverEmail: String,
    val driverLicenseId: String,
    val driverRating: Double,
    val driverProfileImageUrl: String? = null,
    
    val departingTown: String,
    val destination: String,
    val pickupLocation: Location,
    val dropoffLocation: Location? = null,
    
    val departureDate: String, // ISO 8601 date
    val departureTime: String, // HH:mm format
    
    val totalSeats: Int,
    val availableSeats: Int,
    val pricePerSeat: Double,
    
    val vehicleInfo: VehicleInfo,
    
    val status: RideStatus = RideStatus.SCHEDULED,
    val type: RideType = RideType.DRIVER_OFFER,
    
    val allowsParcels: Boolean = true,
    val parcelPricePerKg: Double = 0.0,
    
    val description: String? = null,
    val amenities: List<String> = emptyList(),
    
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val cancelledAt: Long? = null,
    val cancellationReason: String? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class CreateRideRequest(
    val departingTown: String,
    val destination: String,
    val pickupLocation: Location,
    val dropoffLocation: Location? = null,
    val departureDate: String,
    val departureTime: String,
    val totalSeats: Int,
    val pricePerSeat: Double,
    val allowsParcels: Boolean = true,
    val parcelPricePerKg: Double = 0.0,
    val description: String? = null,
    val amenities: List<String> = emptyList()
)

@Serializable
data class RideFilter(
    val departingTown: String? = null,
    val destination: String? = null,
    val date: String? = null,
    val minSeats: Int? = null,
    val maxPrice: Double? = null,
    val minRating: Double? = null
)

@Serializable
data class RideSearchResponse(
    val rides: List<Ride>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)
