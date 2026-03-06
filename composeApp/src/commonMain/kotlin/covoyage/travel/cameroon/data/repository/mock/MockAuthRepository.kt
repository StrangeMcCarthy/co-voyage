package covoyage.travel.cameroon.data.repository.mock

import covoyage.travel.cameroon.data.local.LocalStorageService
import covoyage.travel.cameroon.data.model.UserProfile
import covoyage.travel.cameroon.data.model.UserType
import covoyage.travel.cameroon.data.repository.AuthRepository
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Wrapper to persist user + password pairs.
 */
@Serializable
private data class StoredUser(
    val profile: UserProfile,
    val password: String,
)

@OptIn(ExperimentalUuidApi::class)
class MockAuthRepository(
    private val storage: LocalStorageService,
) : AuthRepository {

    private val users = mutableListOf<StoredUser>()
    private var currentUser: UserProfile? = null

    init {
        val stored = storage.loadList<StoredUser>(KEY_USERS)
        if (stored.isNotEmpty()) {
            users.addAll(stored)
        } else {
            // Seed with sample users
            users.addAll(seedUsers())
            persistUsers()
        }
        // Restore logged-in session
        currentUser = storage.loadObject<UserProfile>(KEY_CURRENT_USER)
    }

    private fun persistUsers() {
        storage.saveList(KEY_USERS, users)
    }

    private fun persistCurrentUser() {
        if (currentUser != null) {
            storage.saveObject(KEY_CURRENT_USER, currentUser!!)
        } else {
            storage.remove(KEY_CURRENT_USER)
        }
    }

    override suspend fun login(email: String, password: String): Result<UserProfile> {
        val found = users.find { it.profile.email == email && it.password == password }
        return if (found != null) {
            currentUser = found.profile
            persistCurrentUser()
            Result.success(found.profile)
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
        payoutPhoneNumber: String,
    ): Result<UserProfile> {
        if (users.any { it.profile.email == email }) {
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
            payoutPhoneNumber = payoutPhoneNumber,
        )
        users.add(StoredUser(newUser, password))
        persistUsers()
        currentUser = newUser
        persistCurrentUser()
        return Result.success(newUser)
    }

    override suspend fun updatePayoutPhoneNumber(number: String): Result<UserProfile> {
        val user = currentUser ?: return Result.failure(Exception("Not logged in"))
        val updatedProfile = user.copy(payoutPhoneNumber = number)
        val index = users.indexOfFirst { it.profile.id == user.id }
        if (index != -1) {
            users[index] = users[index].copy(profile = updatedProfile)
            persistUsers()
            currentUser = updatedProfile
            persistCurrentUser()
            return Result.success(updatedProfile)
        }
        return Result.failure(Exception("User not found"))
    }

    override suspend fun getCurrentUser(): UserProfile? = currentUser

    override suspend fun logout() {
        currentUser = null
        persistCurrentUser()
    }

    override fun isLoggedIn(): Boolean = currentUser != null

    companion object {
        private const val KEY_USERS = "covoyage_users"
        private const val KEY_CURRENT_USER = "covoyage_current_user"

        private fun seedUsers() = listOf(
            StoredUser(
                profile = UserProfile(
                    id = "driver-001",
                    name = "Jean-Pierre Kamga",
                    email = "jpkamga@email.cm",
                    phone = "+237 670 123 456",
                    userType = UserType.DRIVER,
                    drivingPermitNumber = "DL-CM-2024-001",
                    greyCardNumber = "GC-CM-2024-001",
                    payoutPhoneNumber = "+237 670 123 456",
                ),
                password = "password123",
            ),
            StoredUser(
                profile = UserProfile(
                    id = "passenger-001",
                    name = "Marie Fotso",
                    email = "mfotso@email.cm",
                    phone = "+237 680 654 321",
                    userType = UserType.PASSENGER,
                ),
                password = "password123",
            ),
        )
    }
}
