package covoyage.travel.cameroon.data.remote

import covoyage.travel.cameroon.data.model.ApiResponse
import covoyage.travel.cameroon.data.model.RideRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Client-side API service for ride request management.
 * Calls the server's /api/ride-requests endpoints.
 */
class RideRequestApiService(
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

    /** Create a new ride request */
    suspend fun createRequest(request: RideRequest): Result<ApiResponse> = runCatching {
        httpClient.post("$baseUrl/api/ride-requests") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<ApiResponse>()
    }

    /** Get all open ride requests */
    suspend fun getOpenRequests(): Result<List<RideRequest>> = runCatching {
        httpClient.get("$baseUrl/api/ride-requests").body<List<RideRequest>>()
    }

    /** Get ride requests by passenger */
    suspend fun getMyRequests(passengerId: String): Result<List<RideRequest>> = runCatching {
        httpClient.get("$baseUrl/api/ride-requests/passenger/$passengerId").body<List<RideRequest>>()
    }

    /** Close a ride request */
    suspend fun closeRequest(requestId: String, passengerId: String): Result<ApiResponse> = runCatching {
        httpClient.put("$baseUrl/api/ride-requests/$requestId/close") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("passengerId" to passengerId))
        }.body<ApiResponse>()
    }
}
