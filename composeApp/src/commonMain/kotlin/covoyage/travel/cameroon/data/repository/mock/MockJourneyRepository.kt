package covoyage.travel.cameroon.data.repository.mock

import covoyage.travel.cameroon.data.local.LocalStorageService
import covoyage.travel.cameroon.data.model.Journey
import covoyage.travel.cameroon.data.model.JourneyStatus
import covoyage.travel.cameroon.data.remote.DriverPayoutResponse
import covoyage.travel.cameroon.data.remote.PayoutItem
import covoyage.travel.cameroon.data.repository.JourneyRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MockJourneyRepository(
    private val storage: LocalStorageService,
) : JourneyRepository {

    private val journeys = mutableListOf<Journey>()

    init {
        val stored = storage.loadList<Journey>(KEY_JOURNEYS)
        if (stored.isNotEmpty()) {
            journeys.addAll(stored)
        } else {
            // Seed with sample journeys
            journeys.addAll(seedJourneys())
            persist()
        }
    }

    private fun persist() {
        storage.saveList(KEY_JOURNEYS, journeys)
    }

    override suspend fun createJourney(journey: Journey): Result<Journey> {
        val newJourney = journey.copy(
            id = "journey-${Uuid.random().toString().take(8)}",
            createdAt = "2026-02-18T12:00:00Z", // simplified
        )
        journeys.add(0, newJourney)
        persist()
        return Result.success(newJourney)
    }

    override suspend fun getJourneys(): Result<List<Journey>> {
        return Result.success(journeys.filter { it.status == JourneyStatus.SCHEDULED })
    }

    override suspend fun getJourneyById(id: String): Result<Journey> {
        val journey = journeys.find { it.id == id }
        return if (journey != null) {
            Result.success(journey)
        } else {
            Result.failure(Exception("Journey not found"))
        }
    }

    override suspend fun getJourneysByDriver(driverId: String): Result<List<Journey>> {
        return Result.success(journeys.filter { it.driverId == driverId })
    }

    override suspend fun searchJourneys(
        departureCity: String,
        arrivalCity: String,
        date: String
    ): Result<List<Journey>> {
        val results = journeys.filter { journey ->
            journey.status == JourneyStatus.SCHEDULED &&
                (departureCity.isBlank() || journey.departureCity.contains(departureCity, ignoreCase = true)) &&
                (arrivalCity.isBlank() || journey.arrivalCity.contains(arrivalCity, ignoreCase = true)) &&
                (date.isBlank() || journey.departureDate == date)
        }
        return Result.success(results)
    }

    override suspend fun updateJourneyStatus(
        journeyId: String,
        status: JourneyStatus
    ): Result<Journey> {
        val index = journeys.indexOfFirst { it.id == journeyId }
        return if (index >= 0) {
            val updated = journeys[index].copy(status = status)
            journeys[index] = updated
            persist()
            Result.success(updated)
        } else {
            Result.failure(Exception("Journey not found"))
        }
    }

    override suspend fun deleteJourney(journeyId: String): Result<Unit> {
        val removed = journeys.removeAll { it.id == journeyId }
        if (removed) persist()
        return if (removed) Result.success(Unit)
        else Result.failure(Exception("Journey not found"))
    }

    override suspend fun startTrip(journeyId: String): Result<Journey> {
        return updateJourneyStatus(journeyId, JourneyStatus.IN_PROGRESS)
    }

    override suspend fun completeTrip(journeyId: String): Result<Journey> {
        return updateJourneyStatus(journeyId, JourneyStatus.COMPLETED)
    }

    override suspend fun getDriverPayouts(driverId: String): Result<DriverPayoutResponse> {
        // Compute payout summary from completed journeys
        val driverJourneys = journeys.filter { it.driverId == driverId }
        val completedJourneys = driverJourneys.filter { it.status == JourneyStatus.COMPLETED }
        val bookedSeats = completedJourneys.sumOf { it.totalSeats - it.availableSeats }
        val totalEarned = completedJourneys.sumOf { (it.totalSeats - it.availableSeats) * it.pricePerSeat }
        val platformFee = (totalEarned * 0.10).toInt()
        val driverPayout = totalEarned - platformFee

        val payoutItems = completedJourneys.map { journey ->
            val passengers = journey.totalSeats - journey.availableSeats
            val amount = passengers * journey.pricePerSeat
            val fee = (amount * 0.10).toInt()
            PayoutItem(
                paymentId = "mock-${journey.id}",
                journeyId = journey.id,
                passengerName = "$passengers passenger(s)",
                totalAmount = amount,
                driverPayout = amount - fee,
                platformFee = fee,
                paymentMethod = "MTN_MOMO",
                releasedAt = journey.createdAt,
            )
        }

        return Result.success(
            DriverPayoutResponse(
                totalEarned = driverPayout,
                pendingEarnings = 0,
                totalTrips = completedJourneys.size,
                payouts = payoutItems,
            )
        )
    }

    companion object {
        private const val KEY_JOURNEYS = "covoyage_journeys"

        private fun seedJourneys() = listOf(
            Journey(
                id = "journey-001",
                driverId = "driver-001",
                driverName = "Jean-Pierre Kamga",
                driverPhone = "+237 670 123 456",
                departureCity = "Douala",
                arrivalCity = "Yaoundé",
                departureDate = "2026-02-20",
                departureTime = "06:00",
                totalSeats = 4,
                availableSeats = 3,
                pricePerSeat = 5000,
                vehicleName = "Toyota",
                vehicleModel = "Corolla 2020 - White",
                vehiclePlateNumber = "LT 1234 AB",
                additionalNotes = "Air conditioned. Luggage space available.",
                status = JourneyStatus.SCHEDULED,
                createdAt = "2026-02-18T10:00:00Z",
            ),
            Journey(
                id = "journey-002",
                driverId = "driver-001",
                driverName = "Jean-Pierre Kamga",
                driverPhone = "+237 670 123 456",
                departureCity = "Douala",
                arrivalCity = "Buea",
                departureDate = "2026-02-21",
                departureTime = "08:30",
                totalSeats = 3,
                availableSeats = 2,
                pricePerSeat = 3000,
                vehicleName = "Toyota",
                vehicleModel = "Corolla 2020 - White",
                vehiclePlateNumber = "LT 1234 AB",
                additionalNotes = "Quick stop at Tiko possible.",
                status = JourneyStatus.SCHEDULED,
                createdAt = "2026-02-18T10:30:00Z",
            ),
            Journey(
                id = "journey-003",
                driverId = "driver-002",
                driverName = "Paul Njoh",
                driverPhone = "+237 691 555 789",
                departureCity = "Yaoundé",
                arrivalCity = "Bafoussam",
                departureDate = "2026-02-22",
                departureTime = "07:00",
                totalSeats = 4,
                availableSeats = 4,
                pricePerSeat = 4500,
                vehicleName = "Honda",
                vehicleModel = "Civic 2019 - Silver",
                vehiclePlateNumber = "CE 5678 CD",
                additionalNotes = "Comfortable ride. No smoking.",
                status = JourneyStatus.SCHEDULED,
                createdAt = "2026-02-18T11:00:00Z",
            ),
            Journey(
                id = "journey-004",
                driverId = "driver-003",
                driverName = "Aminatou Bello",
                driverPhone = "+237 677 888 111",
                departureCity = "Bamenda",
                arrivalCity = "Douala",
                departureDate = "2026-02-23",
                departureTime = "05:30",
                totalSeats = 3,
                availableSeats = 1,
                pricePerSeat = 7000,
                vehicleName = "Toyota",
                vehicleModel = "Highlander 2021 - Black",
                vehiclePlateNumber = "NW 9012 EF",
                additionalNotes = "Long trip. Rest stop in Bafoussam.",
                status = JourneyStatus.SCHEDULED,
                createdAt = "2026-02-18T12:00:00Z",
            ),
            Journey(
                id = "journey-005",
                driverId = "driver-004",
                driverName = "Christelle Mbarga",
                driverPhone = "+237 699 222 444",
                departureCity = "Douala",
                arrivalCity = "Kribi",
                departureDate = "2026-02-24",
                departureTime = "09:00",
                totalSeats = 4,
                availableSeats = 3,
                pricePerSeat = 4000,
                vehicleName = "Hyundai",
                vehicleModel = "Tucson 2022 - Blue",
                vehiclePlateNumber = "LT 3456 GH",
                additionalNotes = "Beach trip! Surfboards welcome.",
                status = JourneyStatus.SCHEDULED,
                createdAt = "2026-02-18T13:00:00Z",
            ),
        )
    }
}
