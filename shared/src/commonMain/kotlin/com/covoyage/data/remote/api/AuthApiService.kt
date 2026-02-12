package com.covoyage.data.remote.api

import com.covoyage.data.remote.ApiConfig
import com.covoyage.data.remote.ApiResult
import com.covoyage.data.remote.safeApiCall
import com.covoyage.domain.model.AuthResponse
import com.covoyage.domain.model.LoginRequest
import com.covoyage.domain.model.User
import com.covoyage.domain.model.UserRegistrationRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class AuthApiService(private val httpClient: HttpClient) {
    
    suspend fun register(request: UserRegistrationRequest): ApiResult<AuthResponse> = safeApiCall {
        httpClient.post(ApiConfig.Endpoints.REGISTER) {
            setBody(request)
        }.body()
    }
    
    suspend fun login(request: LoginRequest): ApiResult<AuthResponse> = safeApiCall {
        httpClient.post(ApiConfig.Endpoints.LOGIN) {
            setBody(request)
        }.body()
    }
    
    suspend fun refreshToken(refreshToken: String): ApiResult<AuthResponse> = safeApiCall {
        httpClient.post(ApiConfig.Endpoints.REFRESH_TOKEN) {
            setBody(mapOf("refreshToken" to refreshToken))
        }.body()
    }
    
    suspend fun logout(): ApiResult<Unit> = safeApiCall {
        httpClient.post(ApiConfig.Endpoints.LOGOUT).body()
    }
    
    suspend fun getCurrentUser(): ApiResult<User> = safeApiCall {
        httpClient.get("${ApiConfig.Endpoints.USERS}/me").body()
    }
}
