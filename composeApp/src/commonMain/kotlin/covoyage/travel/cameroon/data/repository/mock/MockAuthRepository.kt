package covoyage.travel.cameroon.data.repository.mock

import covoyage.travel.cameroon.data.model.UserProfile
import covoyage.travel.cameroon.data.model.UserType
import covoyage.travel.cameroon.data.repository.AuthRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MockAuthRepository : AuthRepository {

    private val users = mutableListOf<Pair<UserProfile, String>>() // user to password
    private var currentUser: UserProfile? = null

    init {
        // Seed with sample users
        val demoDriver = UserProfile(
            id = "driver-001",
            name = "Jean-Pierre Kamga",
            email = "jpkamga@email.cm",
            phone = "+237 670 123 456",
            userType = UserType.DRIVER,
            drivingPermitNumber = "DL-CM-2024-001",
            greyCardNumber = "GC-CM-2024-001",
        )
        val demoPassenger = UserProfile(
            id = "passenger-001",
            name = "Marie Fotso",
            email = "mfotso@email.cm",
            phone = "+237 680 654 321",
            userType = UserType.PASSENGER,
        )
        users.add(demoDriver to "password123")
        users.add(demoPassenger to "password123")
    }

    override suspend fun login(email: String, password: String): Result<UserProfile> {
        val found = users.find { it.first.email == email && it.second == password }
        return if (found != null) {
            currentUser = found.first
            Result.success(found.first)
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        userType: UserType,
        drivingPermitNumber: String,
        greyCardNumber: String,
    ): Result<UserProfile> {
        if (users.any { it.first.email == email }) {
            return Result.failure(Exception("An account with this email already exists"))
        }

        val newUser = UserProfile(
            id = Uuid.random().toString(),
            name = name,
            email = email,
            phone = phone,
            userType = userType,
            drivingPermitNumber = drivingPermitNumber,
            greyCardNumber = greyCardNumber,
        )
        users.add(newUser to password)
        currentUser = newUser
        return Result.success(newUser)
    }

    override suspend fun getCurrentUser(): UserProfile? = currentUser

    override suspend fun logout() {
        currentUser = null
    }

    override fun isLoggedIn(): Boolean = currentUser != null
}
