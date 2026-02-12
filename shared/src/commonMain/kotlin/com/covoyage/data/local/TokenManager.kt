package com.covoyage.data.local

interface TokenManager {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun saveRefreshToken(token: String)
    suspend fun getRefreshToken(): String?
    suspend fun clearTokens()
}

expect class TokenManagerImpl() : TokenManager {
    override suspend fun saveToken(token: String)
    override suspend fun getToken(): String?
    override suspend fun saveRefreshToken(token: String)
    override suspend fun getRefreshToken(): String?
    override suspend fun clearTokens()
}
