package covoyage.server.service

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import covoyage.server.database.MongoConfig
import covoyage.server.model.*
import covoyage.server.security.JwtConfig
import covoyage.server.security.SecurityUtils
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import org.bson.Document
import java.util.UUID

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

        val userId = "user-${UUID.randomUUID()}"
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
            put("status", "PENDING_VERIFICATION")
            put("createdAt", Clock.System.now().toString())
        }

        users.insertOne(userDoc)

        return AuthResponse(
            success = true,
            message = "Registration successful",
            token = JwtConfig.generateToken(userId, request.userType),
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

        val userId = userDoc.getString("id") ?: ""
        val userType = userDoc.getString("userType") ?: "PASSENGER"

        return AuthResponse(
            success = true,
            message = "Login successful",
            token = JwtConfig.generateToken(userId, userType),
            user = AuthUserData(
                id = userId,
                name = userDoc.getString("name") ?: "",
                email = userDoc.getString("email") ?: "",
                phone = userDoc.getString("phone") ?: "",
                userType = userType,
                drivingPermitNumber = userDoc.getString("drivingPermitNumber") ?: "",
                greyCardNumber = userDoc.getString("greyCardNumber") ?: "",
            ),
        )
    }

    /**
     * Verify specifically for ADMIN users.
     */
    suspend fun verifyAdmin(request: AuthLoginRequest): AuthResponse {
        val trimmedEmail = request.email.trim().lowercase()

        val userDoc = users.find(Filters.eq("email", trimmedEmail)).firstOrNull()
            ?: return AuthResponse(success = false, message = "Invalid email or password")

        val userType = userDoc.getString("userType") ?: "PASSENGER"
        if (userType != "ADMIN") {
            return AuthResponse(success = false, message = "Access denied: Admin role required")
        }

        val storedHash = userDoc.getString("passwordHash") ?: ""
        if (!SecurityUtils.verifyPassword(request.password, storedHash)) {
            return AuthResponse(success = false, message = "Invalid email or password")
        }

        return AuthResponse(
            success = true,
            message = "Admin login successful",
            user = AuthUserData(
                id = userDoc.getString("id") ?: "",
                name = userDoc.getString("name") ?: "",
                email = userDoc.getString("email") ?: "",
                phone = userDoc.getString("phone") ?: "",
                userType = userType,
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

    /**
     * Forgot Password — generates a 6-digit OTP and stores it.
     */
    suspend fun forgotPassword(request: ForgotPasswordRequest, notificationService: NotificationService): AuthResponse {
        val email = request.email.trim().lowercase()
        val userDoc = users.find(Filters.eq("email", email)).firstOrNull()
            ?: return AuthResponse(success = false, message = "No account found with this email")

        val userId = userDoc.getString("id") ?: ""
        val otp = (100000..999999).random().toString()
        val expiry = Clock.System.now().toEpochMilliseconds() + (10 * 60 * 1000) // 10 minutes

        val otps = mongoConfig.database.getCollection<Document>("otps")
        otps.deleteMany(Filters.eq("email", email)) // Clear old OTPs
        otps.insertOne(Document().apply {
            put("email", email)
            put("otp", otp)
            put("expiry", expiry)
        })

        // Deliver OTP via Push Notification (if possible)
        notificationService.sendToUser(
            userId,
            "Password Reset OTP",
            "Your CoVoyage verification code is: $otp",
            mapOf("type" to "otp_delivery", "otp" to otp)
        )

        // OTP is delivered only via push notification / SMS — NOT in the HTTP response
        return AuthResponse(
            success = true,
            message = "OTP sent successfully to your account",
        )
    }

    /**
     * Reset Password — verifies OTP and updates password.
     */
    suspend fun resetPassword(request: ResetPasswordRequest): AuthResponse {
        val email = request.email.trim().lowercase()
        val otps = mongoConfig.database.getCollection<Document>("otps")
        val otpDoc = otps.find(Filters.eq("email", email)).firstOrNull()
            ?: return AuthResponse(success = false, message = "No reset request found for this email")

        val storedOtp = otpDoc.getString("otp")
        val expiry = otpDoc.getLong("expiry") ?: 0L

        if (storedOtp != request.otp) {
            return AuthResponse(success = false, message = "Invalid OTP")
        }
        if (Clock.System.now().toEpochMilliseconds() > expiry) {
            otps.deleteOne(Filters.eq("email", email))
            return AuthResponse(success = false, message = "OTP has expired")
        }

        if (request.newPassword.length < 6) {
            return AuthResponse(success = false, message = "New password must be at least 6 characters")
        }

        // Update password
        val newHash = SecurityUtils.hashPassword(request.newPassword)
        users.updateOne(
            Filters.eq("email", email),
            Updates.combine(
                Updates.set("passwordHash", newHash),
                Updates.set("updatedAt", Clock.System.now().toString()),
            ),
        )

        // Clean up OTP
        otps.deleteOne(Filters.eq("email", email))

        return AuthResponse(success = true, message = "Password reset successfully")
    }

    /**
     * Request Phone OTP — generates and sends a 6-digit OTP via SMS.
     */
    suspend fun requestPhoneOtp(userId: String, phone: String, notificationService: NotificationService): AuthResponse {
        val userDoc = users.find(Filters.eq("id", userId)).firstOrNull()
            ?: return AuthResponse(success = false, message = "User not found")

        val otp = (100000..999999).random().toString()
        val expiry = Clock.System.now().toEpochMilliseconds() + (10 * 60 * 1000) // 10 minutes

        val otps = mongoConfig.database.getCollection<Document>("otps")
        otps.deleteMany(Filters.eq("userId", userId)) // Clear old OTPs
        otps.insertOne(Document().apply {
            put("userId", userId)
            put("otp", otp)
            put("expiry", expiry)
            put("type", "PHONE_VERIFICATION")
        })

        // Deliver OTP via Mock SMS
        notificationService.sendSms(phone, "Your CoVoyage verification code is: $otp")

        return AuthResponse(success = true, message = "OTP sent successfully to your phone")
    }

    /**
     * Verify Phone OTP — checks code and activates user.
     */
    suspend fun verifyPhoneOtp(userId: String, otp: String): AuthResponse {
        val otps = mongoConfig.database.getCollection<Document>("otps")
        val otpDoc = otps.find(Filters.and(
            Filters.eq("userId", userId),
            Filters.eq("type", "PHONE_VERIFICATION")
        )).firstOrNull() ?: return AuthResponse(success = false, message = "No verification request found")

        val storedOtp = otpDoc.getString("otp")
        val expiry = otpDoc.getLong("expiry") ?: 0L

        if (storedOtp != otp) {
            return AuthResponse(success = false, message = "Invalid OTP")
        }
        if (Clock.System.now().toEpochMilliseconds() > expiry) {
            otps.deleteOne(Filters.eq("userId", userId))
            return AuthResponse(success = false, message = "OTP has expired")
        }

        // Activate user
        users.updateOne(
            Filters.eq("id", userId),
            Updates.set("status", "ACTIVE")
        )

        // Clean up
        otps.deleteOne(Filters.eq("userId", userId))

        return AuthResponse(success = true, message = "Phone verified successfully")
    }
}
