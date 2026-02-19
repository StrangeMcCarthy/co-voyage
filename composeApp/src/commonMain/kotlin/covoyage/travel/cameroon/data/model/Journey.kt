package covoyage.travel.cameroon.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Journey(
    val id: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val departureCity: String = "",
    val arrivalCity: String = "",
    val departureDate: String = "",   // ISO date string e.g. "2026-03-15"
    val departureTime: String = "",   // e.g. "08:00"
    val totalSeats: Int = 4,
    val availableSeats: Int = 4,
    val pricePerSeat: Int = 0,        // in XAF (CFA Francs)
    val currency: String = "XAF",
    val vehicleName: String = "",     // e.g. "Toyota"
    val vehicleModel: String = "",    // e.g. "Corolla 2020"
    val vehiclePlateNumber: String = "",
    val additionalNotes: String = "",
    val status: JourneyStatus = JourneyStatus.SCHEDULED,
    val createdAt: String = "",       // ISO timestamp
)

@Serializable
enum class JourneyStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

/**
 * Popular intercity routes in Cameroon for search suggestions.
 */
object CameroonCities {
    val cities = listOf(
        "Douala", "Yaoundé", "Bamenda", "Bafoussam", "Garoua",
        "Maroua", "Kumba", "Nkongsamba", "Buea", "Limbe",
        "Bertoua", "Ebolowa", "Ngaoundéré", "Kribi", "Dschang",
        "Foumban", "Edéa", "Loum", "Mbalmayo", "Sangmélima",
        "Tiko", "Kumbo", "Wum", "Mamfe", "Mbouda"
    )

    val popularRoutes = listOf(
        "Douala" to "Yaoundé",
        "Douala" to "Buea",
        "Douala" to "Bafoussam",
        "Yaoundé" to "Douala",
        "Yaoundé" to "Bafoussam",
        "Bamenda" to "Douala",
        "Bamenda" to "Bafoussam",
        "Douala" to "Limbe",
        "Douala" to "Kribi",
        "Yaoundé" to "Kribi",
    )
}
