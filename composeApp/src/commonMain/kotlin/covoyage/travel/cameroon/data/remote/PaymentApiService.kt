package covoyage.travel.cameroon.data.remote

import covoyage.travel.cameroon.data.model.ApiResponse
import covoyage.travel.cameroon.data.model.InitiatePaymentRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Client-side service calling the CoVoyage server's payment endpoints.
 * This does NOT talk to Flutterwave directly — all payment logic goes through our server.
 */
class PaymentApiService(
    private val baseUrl: String = "http://10.0.2.2:8080", // Android emulator → host
) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /**
     * Initiate a payment via the server.
     * Server calls Flutterwave and returns a payment reference.
     */
    suspend fun initiatePayment(request: InitiatePaymentRequest): Result<ApiResponse> {
        return try {
            val response = httpClient.post("$baseUrl/api/payments/initiate") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            Result.success(response.body<ApiResponse>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Poll payment status.
     * Used after initiating MoMo/OM charge to check if user approved.
     */
    suspend fun getPaymentStatus(paymentId: String): Result<ApiResponse> {
        return try {
            val response = httpClient.get("$baseUrl/api/payments/$paymentId/status")
            Result.success(response.body<ApiResponse>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Release escrow payment to driver (after trip completion).
     */
    suspend fun releasePayment(paymentId: String): Result<ApiResponse> {
        return try {
            val response = httpClient.post("$baseUrl/api/payments/$paymentId/release")
            Result.success(response.body<ApiResponse>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
