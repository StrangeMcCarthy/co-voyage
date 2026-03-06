package covoyage.travel.cameroon.data.repository

import covoyage.travel.cameroon.data.model.UserProfile
import covoyage.travel.cameroon.data.model.UserType

/**
 * Repository for authentication and user management.
 */
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserProfile>
    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        userType: UserType,
        drivingPermitNumber: String = "",
        greyCardNumber: String = "",
        payoutPhoneNumber: String = "",
    ): Result<UserProfile>
    suspend fun getCurrentUser(): UserProfile?
    suspend fun updatePayoutPhoneNumber(number: String): Result<UserProfile>
    suspend fun logout()
    fun isLoggedIn(): Boolean
    
    // Forgot Password
    suspend fun forgotPassword(email: String): Result<String> // Returns the OTP (for testing/demo)
    suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<Boolean>
}
