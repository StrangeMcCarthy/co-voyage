package covoyage.travel.cameroon.data.repository

import covoyage.travel.cameroon.data.model.RideRequest

/**
 * Repository for passenger ride request management.
 */
interface RideRequestRepository {
    suspend fun createRequest(request: RideRequest): Result<RideRequest>
    suspend fun getOpenRequests(): Result<List<RideRequest>>
    suspend fun getMyRequests(passengerId: String): Result<List<RideRequest>>
    suspend fun closeRequest(requestId: String, passengerId: String): Result<Unit>
}
