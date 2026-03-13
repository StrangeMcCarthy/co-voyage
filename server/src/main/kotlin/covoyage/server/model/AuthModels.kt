package covoyage.server.model

import kotlinx.serialization.Serializable

/**
 * Request / response models for authentication endpoints.
 */
@Serializable
data class AuthRegisterRequest(
    val name: String,
    val email: String,
    val phone: String = "",
    val password: String,
    val userType: String = "PASSENGER",
    val drivingPermitNumber: String = "",
    val greyCardNumber: String = "",
)

@Serializable
data class AuthLoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class ChangePasswordRequest(
    val userId: String,
    val oldPassword: String,
    val newPassword: String,
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: AuthUserData? = null,
    val token: String? = null,
    val otp: String? = null, // Temporary for development/testing if needed
)

@Serializable
data class ForgotPasswordRequest(
    val email: String,
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String,
)

@Serializable
data class PhoneOtpRequest(
    val userId: String,
    val phone: String,
)

@Serializable
data class VerifyPhoneOtpRequest(
    val userId: String,
    val otp: String,
)

@Serializable
data class AuthUserData(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val userType: String = "PASSENGER",
    val drivingPermitNumber: String = "",
    val greyCardNumber: String = "",
    val status: String = "ACTIVE",
)
