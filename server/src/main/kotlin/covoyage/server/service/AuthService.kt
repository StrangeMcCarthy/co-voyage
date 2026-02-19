package covoyage.server.service

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import covoyage.server.database.MongoConfig
import covoyage.server.model.*
import covoyage.server.security.SecurityUtils
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import org.bson.Document

/**
 * Server-side authentication service.
 * Passwords are BCrypt-hashed before storage — never stored in plaintext.
 */
class AuthService(private val mongoConfig: MongoConfig) {

    private val users get() = mongoConfig.database.getCollection<Document>("users")

    /**
     * Register a new user. BCrypt-hashes the password before persisting.
     */
    suspend fun register(request: AuthRegisterRequest): AuthResponse {
        val email = request.email.trim().lowercase()

        if (email.isBlank() || request.password.isBlank() || request.name.isBlank()) {
            return AuthResponse(success = false, message = "Name, email and password are required")
        }
        if (request.password.length < 6) {
            return AuthResponse(success = false, message = "Password must be at least 6 characters")
        }

        // Check for existing user
        val existing = users.find(Filters.eq("email", email)).firstOrNull()
        if (existing != null) {
            return AuthResponse(success = false, message = "An account with this email already exists")
        }

        val userId = "user-${Clock.System.now().toEpochMilliseconds()}"
        val hashedPassword = SecurityUtils.hashPassword(request.password)

        val userDoc = Document().apply {
            put("id", userId)
            put("name", request.name)
            put("email", email)
            put("phone", request.phone)
            put("userType", request.userType)
            put("passwordHash", hashedPassword)
            put("drivingPermitNumber", request.drivingPermitNumber)
            put("greyCardNumber", request.greyCardNumber)
            put("savedVehicles", emptyList<Document>())
            put("createdAt", Clock.System.now().toString())
        }

        users.insertOne(userDoc)

        return AuthResponse(
            success = true,
            message = "Registration successful",
            user = AuthUserData(
                id = userId,
                name = request.name,
                email = email,
                phone = request.phone,
                userType = request.userType,
                drivingPermitNumber = request.drivingPermitNumber,
                greyCardNumber = request.greyCardNumber,
            ),
        )
    }

    /**
     * Login — fetches user by email, then BCrypt-verifies the password.
     * Returns user profile data (never the hash).
     */
    suspend fun login(request: AuthLoginRequest): AuthResponse {
        val trimmedEmail = request.email.trim().lowercase()
        val userDoc = users.find(Filters.eq("email", trimmedEmail)).firstOrNull()
            ?: return AuthResponse(success = false, message = "Invalid email or password")

        val storedHash = userDoc.getString("passwordHash") ?: ""
        if (!SecurityUtils.verifyPassword(request.password, storedHash)) {
            return AuthResponse(success = false, message = "Invalid email or password")
        }

        return AuthResponse(
            success = true,
            message = "Login successful",
            user = AuthUserData(
                id = userDoc.getString("id") ?: "",
                name = userDoc.getString("name") ?: "",
                email = userDoc.getString("email") ?: "",
                phone = userDoc.getString("phone") ?: "",
                userType = userDoc.getString("userType") ?: "PASSENGER",
                drivingPermitNumber = userDoc.getString("drivingPermitNumber") ?: "",
                greyCardNumber = userDoc.getString("greyCardNumber") ?: "",
            ),
        )
    }

    /**
     * Change password — verifies old password, then BCrypt-hashes the new one.
     */
    suspend fun changePassword(request: ChangePasswordRequest): AuthResponse {
        val userDoc = users.find(Filters.eq("id", request.userId)).firstOrNull()
            ?: return AuthResponse(success = false, message = "User not found")

        val storedHash = userDoc.getString("passwordHash") ?: ""
        if (!SecurityUtils.verifyPassword(request.oldPassword, storedHash)) {
            return AuthResponse(success = false, message = "Current password is incorrect")
        }
        if (request.newPassword.length < 6) {
            return AuthResponse(success = false, message = "New password must be at least 6 characters")
        }

        val newHash = SecurityUtils.hashPassword(request.newPassword)
        users.updateOne(
            Filters.eq("id", request.userId),
            Updates.combine(
                Updates.set("passwordHash", newHash),
                Updates.set("updatedAt", Clock.System.now().toString()),
            ),
        )

        return AuthResponse(success = true, message = "Password changed successfully")
    }
}
