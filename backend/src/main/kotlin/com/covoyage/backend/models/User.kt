package com.covoyage.backend.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.mindrot.jbcrypt.BCrypt

@Serializable
enum class UserRole {
    PASSENGER,
    DRIVER,
    ADMIN
}

@Serializable
enum class UserStatus {
    PENDING_VERIFICATION,
    VERIFIED,
    SUSPENDED,
    BANNED
}

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
data class User(
    @BsonId
    val id: String = ObjectId().toHexString(),
    val name: String,
    val email: String,
    @kotlinx.serialization.Transient
    val passwordHash: String = "",
    val phoneNumber: String,
    val town: String,
    val role: UserRole,
    val status: UserStatus = UserStatus.PENDING_VERIFICATION,
    val profileImageUrl: String? = null,
    
    // Driver specific fields
    val driverLicenseNumber: String? = null,
    val idNumber: String? = null,
    val vehicleInfo: VehicleInfo? = null,
    val bankAccountInfo: BankAccountInfo? = null,
    
    // Ratings
    val averageRating: Double = 0.0,
    val totalRatings: Int = 0,
    val totalRides: Int = 0,
    
    // Verification
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false,
    
    // Security
    val lastLoginAt: Long? = null,
    @kotlinx.serialization.Transient
    val refreshToken: String? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun hashPassword(password: String): String {
            return BCrypt.hashpw(password, BCrypt.gensalt(12))
        }
        
        fun verifyPassword(password: String, hashedPassword: String): Boolean {
            return BCrypt.checkpw(password, hashedPassword)
        }
    }
    
    fun toResponse(): UserResponse {
        return UserResponse(
            id = id,
            name = name,
            email = email,
            phoneNumber = phoneNumber,
            town = town,
            role = role,
            status = status,
            profileImageUrl = profileImageUrl,
            driverLicenseNumber = driverLicenseNumber,
            idNumber = idNumber,
            vehicleInfo = vehicleInfo,
            bankAccountInfo = bankAccountInfo,
            averageRating = averageRating,
            totalRatings = totalRatings,
            totalRides = totalRides,
            emailVerified = emailVerified,
            phoneVerified = phoneVerified,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

@Serializable
data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val town: String,
    val role: UserRole,
    val status: UserStatus,
    val profileImageUrl: String? = null,
    val driverLicenseNumber: String? = null,
    val idNumber: String? = null,
    val vehicleInfo: VehicleInfo? = null,
    val bankAccountInfo: BankAccountInfo? = null,
    val averageRating: Double,
    val totalRatings: Int,
    val totalRides: Int,
    val emailVerified: Boolean,
    val phoneVerified: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val town: String,
    val role: UserRole,
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
    val user: UserResponse
)
