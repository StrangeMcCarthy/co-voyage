package covoyage.travel.cameroon.data.repository

import covoyage.travel.cameroon.data.model.Journey
import covoyage.travel.cameroon.data.remote.DriverPayoutResponse

/**
 * Repository for journey (trip post) management.
 */
interface JourneyRepository {
    suspend fun createJourney(journey: Journey): Result<Journey>
    suspend fun getJourneys(): Result<List<Journey>>
    suspend fun getJourneyById(id: String): Result<Journey>
    suspend fun getJourneysByDriver(driverId: String): Result<List<Journey>>
    suspend fun searchJourneys(
        departureCity: String = "",
        arrivalCity: String = "",
        date: String = ""
    ): Result<List<Journey>>
    suspend fun updateJourneyStatus(
        journeyId: String,
        status: covoyage.travel.cameroon.data.model.JourneyStatus
    ): Result<Journey>
    suspend fun deleteJourney(journeyId: String): Result<Unit>

    // Driver-specific operations
    suspend fun startTrip(journeyId: String): Result<Journey>
    suspend fun completeTrip(journeyId: String): Result<Journey>
    suspend fun getDriverPayouts(driverId: String): Result<DriverPayoutResponse>
}
