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
    ): Result<UserProfile>
    suspend fun getCurrentUser(): UserProfile?
    suspend fun logout()
    fun isLoggedIn(): Boolean
}
