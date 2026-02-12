package com.covoyage.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    PASSENGER,
    DRIVER
}

@Serializable
enum class UserStatus {
    PENDING_VERIFICATION,
    VERIFIED,
    SUSPENDED,
    BANNED
}

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val town: String,
    val role: UserRole,
    val status: UserStatus,
    val profileImageUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    
    // Driver specific fields
    val driverLicenseNumber: String? = null,
    val idNumber: String? = null,
    val vehicleInfo: VehicleInfo? = null,
    val bankAccountInfo: BankAccountInfo? = null,
    
    // Ratings
    val averageRating: Double = 0.0,
    val totalRatings: Int = 0,
    val totalRides: Int = 0
)

@Serializable
data class VehicleInfo(
    val make: String,
    val model: String,
    val year: Int,
    val color: String,
    val plateNumber: String,
    val capacity: Int,
    val imageUrl: String? = null
)

@Serializable
data class BankAccountInfo(
    val accountNumber: String,
    val bankName: String,
    val accountHolderName: String,
    val isVerified: Boolean = false
)

@Serializable
data class UserRegistrationRequest(
    val name: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val town: String,
    val role: UserRole,
    
    // Driver specific
    val driverLicenseNumber: String? = null,
    val idNumber: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val user: User
)
