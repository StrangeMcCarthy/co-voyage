package com.covoyage.data.remote

import kotlinx.serialization.Serializable

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val exception: ApiException) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

sealed class ApiException(message: String) : Exception(message) {
    data class NetworkError(val errorMessage: String) : ApiException(errorMessage)
    data class ServerError(val code: Int, val errorMessage: String) : ApiException(errorMessage)
    data class UnauthorizedError(val errorMessage: String = "Unauthorized") : ApiException(errorMessage)
    data class ValidationError(val errors: Map<String, List<String>>) : ApiException("Validation failed")
    data class UnknownError(val errorMessage: String) : ApiException(errorMessage)
}

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errors: Map<String, List<String>>? = null
)

@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val message: String,
    val errors: Map<String, List<String>>? = null,
    val code: String? = null
)

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (e: Exception) {
        ApiResult.Error(
            when (e) {
                is ApiException -> e
                else -> ApiException.UnknownError(e.message ?: "Unknown error occurred")
            }
        )
    }
}
