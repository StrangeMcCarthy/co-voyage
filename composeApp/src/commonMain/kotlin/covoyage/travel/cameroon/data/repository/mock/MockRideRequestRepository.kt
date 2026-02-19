package covoyage.travel.cameroon.data.repository.mock

import covoyage.travel.cameroon.data.model.RideRequest
import covoyage.travel.cameroon.data.model.RideRequestStatus
import covoyage.travel.cameroon.data.repository.RideRequestRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MockRideRequestRepository : RideRequestRepository {

    private val requests = mutableListOf<RideRequest>()

    init {
        // Seed with sample ride requests
        requests.addAll(
            listOf(
                RideRequest(
                    id = "request-001",
                    passengerId = "passenger-001",
                    passengerName = "Marie Fotso",
                    passengerPhone = "+237 691 234 567",
                    departureCity = "Douala",
                    destinationCity = "Yaoundé",
                    travelDate = "2026-02-25",
                    seatsNeeded = 2,
                    message = "Travelling with luggage, prefer AC vehicle.",
                    status = RideRequestStatus.OPEN,
                    createdAt = "2026-02-19T08:00:00Z",
                ),
                RideRequest(
                    id = "request-002",
                    passengerId = "passenger-002",
                    passengerName = "Emmanuel Tabi",
                    passengerPhone = "+237 677 444 222",
                    departureCity = "Bamenda",
                    destinationCity = "Douala",
                    travelDate = "2026-02-26",
                    seatsNeeded = 1,
                    message = "Early morning departure preferred.",
                    status = RideRequestStatus.OPEN,
                    createdAt = "2026-02-19T09:00:00Z",
                ),
                RideRequest(
                    id = "request-003",
                    passengerId = "passenger-003",
                    passengerName = "Claudine Mbarga",
                    passengerPhone = "+237 699 888 333",
                    departureCity = "Yaoundé",
                    destinationCity = "Kribi",
                    travelDate = "2026-02-28",
                    seatsNeeded = 3,
                    message = "Weekend getaway with friends 🏖️",
                    status = RideRequestStatus.OPEN,
                    createdAt = "2026-02-19T10:00:00Z",
                ),
                RideRequest(
                    id = "request-004",
                    passengerId = "passenger-001",
                    passengerName = "Marie Fotso",
                    passengerPhone = "+237 691 234 567",
                    departureCity = "Douala",
                    destinationCity = "Buea",
                    travelDate = "2026-03-01",
                    seatsNeeded = 1,
                    message = "",
                    status = RideRequestStatus.OPEN,
                    createdAt = "2026-02-19T11:00:00Z",
                ),
            )
        )
    }

    override suspend fun createRequest(request: RideRequest): Result<RideRequest> {
        val newRequest = request.copy(
            id = "request-${Uuid.random().toString().take(8)}",
            createdAt = "2026-02-19T12:00:00Z", // simplified
        )
        requests.add(0, newRequest)
        return Result.success(newRequest)
    }

    override suspend fun getOpenRequests(): Result<List<RideRequest>> {
        return Result.success(requests.filter { it.status == RideRequestStatus.OPEN })
    }

    override suspend fun getMyRequests(passengerId: String): Result<List<RideRequest>> {
        return Result.success(requests.filter { it.passengerId == passengerId })
    }

    override suspend fun closeRequest(requestId: String, passengerId: String): Result<Unit> {
        val index = requests.indexOfFirst { it.id == requestId }
        return if (index >= 0) {
            val req = requests[index]
            if (req.passengerId != passengerId) {
                Result.failure(Exception("Unauthorized — not your request"))
            } else {
                requests[index] = req.copy(status = RideRequestStatus.CLOSED)
                Result.success(Unit)
            }
        } else {
            Result.failure(Exception("Request not found"))
        }
    }
}
