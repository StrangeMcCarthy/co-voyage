package covoyage.travel.cameroon.data.remote

import covoyage.travel.cameroon.data.model.ApiResponse
import covoyage.travel.cameroon.data.model.Journey
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Client-side API service for journey management.
 * Calls the server's /api/journeys endpoints.
 */
class JourneyApiService(
    private val baseUrl: String = "http://10.0.2.2:8080",
) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /** Create a new journey */
    suspend fun createJourney(journey: Journey): Result<ApiResponse> = runCatching {
        httpClient.post("$baseUrl/api/journeys") {
            contentType(ContentType.Application.Json)
            setBody(journey)
        }.body<ApiResponse>()
    }

    /** Get driver's journeys */
    suspend fun getDriverJourneys(driverId: String): Result<List<Journey>> = runCatching {
        httpClient.get("$baseUrl/api/journeys/driver/$driverId").body<List<Journey>>()
    }

    /** Edit a journey */
    suspend fun editJourney(journeyId: String, driverId: String, journey: Journey): Result<ApiResponse> = runCatching {
        httpClient.put("$baseUrl/api/journeys/$journeyId") {
            contentType(ContentType.Application.Json)
            setBody(journey.copy(driverId = driverId))
        }.body<ApiResponse>()
    }

    /** Cancel a journey */
    suspend fun cancelJourney(journeyId: String, driverId: String): Result<ApiResponse> = runCatching {
        httpClient.delete("$baseUrl/api/journeys/$journeyId?driverId=$driverId").body<ApiResponse>()
    }

    /** Start a trip */
    suspend fun startTrip(journeyId: String, driverId: String): Result<ApiResponse> = runCatching {
        httpClient.post("$baseUrl/api/journeys/$journeyId/start") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("driverId" to driverId))
        }.body<ApiResponse>()
    }

    /** Complete a trip (triggers escrow release) */
    suspend fun completeTrip(journeyId: String, driverId: String): Result<ApiResponse> = runCatching {
        httpClient.post("$baseUrl/api/journeys/$journeyId/complete") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("driverId" to driverId))
        }.body<ApiResponse>()
    }

    /** Get driver payout history */
    suspend fun getDriverPayouts(driverId: String): Result<DriverPayoutResponse> = runCatching {
        httpClient.get("$baseUrl/api/journeys/payouts/$driverId").body<DriverPayoutResponse>()
    }
}

@Serializable
data class DriverPayoutResponse(
    val totalEarned: Int = 0,
    val pendingEarnings: Int = 0,
    val totalTrips: Int = 0,
    val payouts: List<PayoutItem> = emptyList(),
)

@Serializable
data class PayoutItem(
    val paymentId: String = "",
    val journeyId: String = "",
    val passengerName: String = "",
    val totalAmount: Int = 0,
    val driverPayout: Int = 0,
    val platformFee: Int = 0,
    val paymentMethod: String = "",
    val releasedAt: String = "",
)
