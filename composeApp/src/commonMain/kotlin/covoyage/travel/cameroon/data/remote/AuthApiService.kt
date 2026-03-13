package covoyage.travel.cameroon.data.remote

import covoyage.travel.cameroon.data.model.ApiResponse
import covoyage.travel.cameroon.data.model.UserProfile
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Client-side API service for authentication.
 * Calls the server's /api/auth endpoints.
 */
class AuthApiService(
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

    suspend fun login(email: String, password: String): Result<AuthResponse> = runCatching {
        httpClient.post("$baseUrl/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email, "password" to password))
        }.body<AuthResponse>()
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        userType: String,
        drivingPermitNumber: String = "",
        greyCardNumber: String = "",
    ): Result<AuthResponse> = runCatching {
        httpClient.post("$baseUrl/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
                "password" to password,
                "userType" to userType,
                "drivingPermitNumber" to drivingPermitNumber,
                "greyCardNumber" to greyCardNumber
            ))
        }.body<AuthResponse>()
    }

    suspend fun forgotPassword(email: String): Result<AuthResponse> = runCatching {
        httpClient.post("$baseUrl/api/auth/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email))
        }.body<AuthResponse>()
    }

    suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<AuthResponse> = runCatching {
        httpClient.post("$baseUrl/api/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "email" to email,
                "otp" to otp,
                "newPassword" to newPassword
            ))
        }.body<AuthResponse>()
    }
}

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: AuthUserData? = null,
    val token: String? = null,
    val otp: String? = null,
)

@Serializable
data class AuthUserData(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val userType: String = "",
    val drivingPermitNumber: String = "",
    val greyCardNumber: String = "",
    val status: String = "ACTIVE",
)
