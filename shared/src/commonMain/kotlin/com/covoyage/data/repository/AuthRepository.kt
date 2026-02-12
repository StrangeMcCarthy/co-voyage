package com.covoyage.data.repository

import com.covoyage.data.local.TokenManager
import com.covoyage.data.remote.ApiResult
import com.covoyage.data.remote.api.AuthApiService
import com.covoyage.domain.model.AuthResponse
import com.covoyage.domain.model.LoginRequest
import com.covoyage.domain.model.User
import com.covoyage.domain.model.UserRegistrationRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AuthRepository {
    suspend fun register(request: UserRegistrationRequest): Flow<ApiResult<AuthResponse>>
    suspend fun login(request: LoginRequest): Flow<ApiResult<AuthResponse>>
    suspend fun logout(): Flow<ApiResult<Unit>>
    suspend fun getCurrentUser(): Flow<ApiResult<User>>
    suspend fun isLoggedIn(): Boolean
    suspend fun getToken(): String?
}

class AuthRepositoryImpl(
    private val authApi: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {
    
    override suspend fun register(request: UserRegistrationRequest): Flow<ApiResult<AuthResponse>> = flow {
        emit(ApiResult.Loading)
        val result = authApi.register(request)
        
        if (result is ApiResult.Success) {
            tokenManager.saveToken(result.data.token)
            tokenManager.saveRefreshToken(result.data.refreshToken)
        }
        
        emit(result)
    }
    
    override suspend fun login(request: LoginRequest): Flow<ApiResult<AuthResponse>> = flow {
        emit(ApiResult.Loading)
        val result = authApi.login(request)
        
        if (result is ApiResult.Success) {
            tokenManager.saveToken(result.data.token)
            tokenManager.saveRefreshToken(result.data.refreshToken)
        }
        
        emit(result)
    }
    
    override suspend fun logout(): Flow<ApiResult<Unit>> = flow {
        emit(ApiResult.Loading)
        val result = authApi.logout()
        
        tokenManager.clearTokens()
        
        emit(result)
    }
    
    override suspend fun getCurrentUser(): Flow<ApiResult<User>> = flow {
        emit(ApiResult.Loading)
        emit(authApi.getCurrentUser())
    }
    
    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.getToken() != null
    }
    
    override suspend fun getToken(): String? {
        return tokenManager.getToken()
    }
}
