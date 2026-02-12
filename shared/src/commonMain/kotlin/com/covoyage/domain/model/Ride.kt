package com.covoyage.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class RideStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

@Serializable
enum class RideType {
    PASSENGER_REQUEST,  // Passenger looking for ride
    DRIVER_OFFER        // Driver offering ride
}

@Serializable
data class Ride(
    val id: String,
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
    val dropoffLocation: Location?,
    
    val departureDate: String, // ISO 8601 format
    val departureTime: String, // HH:mm format
    
    val totalSeats: Int,
    val availableSeats: Int,
    val pricePerSeat: Double,
    
    val vehicleInfo: VehicleInfo,
    
    val status: RideStatus,
    val type: RideType,
    
    val allowsParcels: Boolean = true,
    val parcelPricePerKg: Double = 0.0,
    
    val createdAt: Long,
    val updatedAt: Long,
    
    // Additional info
    val description: String? = null,
    val amenities: List<String> = emptyList(), // AC, Music, WiFi, etc.
)

@Serializable
data class Location(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
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
data class PassengerRequest(
    val id: String,
    val passengerId: String,
    val passengerName: String,
    val passengerPhone: String,
    val passengerRating: Double,
    
    val departingTown: String,
    val destination: String,
    val preferredDate: String,
    val preferredTime: String?,
    
    val numberOfSeats: Int,
    val maxPricePerSeat: Double?,
    
    val description: String? = null,
    val createdAt: Long
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
