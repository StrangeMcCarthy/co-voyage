package covoyage.travel.cameroon.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Journey(
    val id: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val departureCity: String = "",
    val departurePoint: String = "", // e.g. "Total Shell Mvan"
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
        "Abong-Mbang", "Akonolinga", "Ambam", "Bafang", "Bafia",
        "Bafoussam", "Baham", "Bali", "Bambui", "Bamenda",
        "Bangangté", "Bankim", "Banyo", "Batibo", "Batouri",
        "Belel", "Bélabo", "Bertoua", "Bibémi", "Blangoua",
        "Bogo", "Buea", "Campo", "Dimako", "Dizangué",
        "Djoum", "Douala", "Dschang", "Ebolowa", "Edéa",
        "Eséka", "Figuil", "Fontem", "Foumban", "Foumbot",
        "Fundong", "Garoua", "Garoua-Boulaï", "Guider", "Guidiguis",
        "Kaélé", "Kribi", "Kumba", "Kumbo", "Kousséri",
        "Lagdo", "Limbe", "Lolodorf", "Loum", "Mamfe",
        "Manjo", "Martap", "Maroua", "Mbalmayo", "Mbandjock",
        "Mbanga", "Mbouda", "Meiganga", "Melong", "Meyomessala",
        "Mokolo", "Moloundou", "Monatélé", "Mora", "Mundemba",
        "Mutengene", "Muyuka", "Nanga Eboko", "Ndop", "Ngaoundéré",
        "Nkambe", "Nkongsamba", "Nkoteng", "Obala", "Penja",
        "Pitoa", "Poli", "Rey Bouba", "Sangmélima", "Santa",
        "Tibati", "Tiko", "Wum", "Yagoua", "Yaoundé",
        "Yokadouma"
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
