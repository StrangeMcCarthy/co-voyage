package covoyage.travel.cameroon.data.model

import kotlinx.serialization.Serializable

/**
 * A passenger-posted ride request.
 * "I need a ride from X to Y on date Z — any driver available?"
 */
@Serializable
data class RideRequest(
    val id: String = "",
    val passengerId: String = "",
    val passengerName: String = "",
    val passengerPhone: String = "",
    val departureCity: String = "",
    val destinationCity: String = "",
    val travelDate: String = "",       // ISO date e.g. "2026-03-01"
    val seatsNeeded: Int = 1,
    val message: String = "",          // optional note from passenger
    val status: RideRequestStatus = RideRequestStatus.OPEN,
    val createdAt: String = "",        // ISO timestamp
)

@Serializable
enum class RideRequestStatus {
    OPEN,
    CLOSED,
    EXPIRED,
}
