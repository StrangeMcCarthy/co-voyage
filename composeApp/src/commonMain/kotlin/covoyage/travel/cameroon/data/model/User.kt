package covoyage.travel.cameroon.data.model

import kotlinx.serialization.Serializable

/**
 * A vehicle saved by a driver for quick reuse when posting rides.
 */
@Serializable
data class SavedVehicle(
    val name: String = "",        // e.g. "Toyota"
    val model: String = "",       // e.g. "Corolla 2020"
    val plateNumber: String = "", // e.g. "LT 1234 AB"
    val seats: Int = 4,
)

/**
 * Base user properties shared by both Driver and Passenger.
 */
@Serializable
data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val userType: UserType = UserType.PASSENGER,
    // Driver-specific fields (empty for passengers)
    val drivingPermitNumber: String = "",
    val greyCardNumber: String = "",
    val profileImageUrl: String = "",
    val savedVehicles: List<SavedVehicle> = emptyList(),
)

@Serializable
enum class UserType {
    DRIVER,
    PASSENGER
}
