package com.covoyage.data.remote

import com.covoyage.data.local.TokenManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class HttpClientFactory(private val tokenManager: TokenManager) {

    fun create(): HttpClient {
        return HttpClient {
            // JSON serialization
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            // Logging
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }

            // Default request configuration
            install(DefaultRequest) {
                url(ApiConfig.BASE_URL)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }

            // Timeout configuration
            install(HttpTimeout) {
                requestTimeoutMillis = ApiConfig.READ_TIMEOUT
                connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT
                socketTimeoutMillis = ApiConfig.READ_TIMEOUT
            }

            // Authentication with token refresh
            install(Auth) {
                bearer {
                    loadTokens {
                        val accessToken = tokenManager.getToken()
                        val refreshToken = tokenManager.getRefreshToken()
                        if (accessToken != null && refreshToken != null) {
                            BearerTokens(accessToken, refreshToken)
                        } else {
                            null
                        }
                    }

                    refreshTokens {
                        val refreshToken = tokenManager.getRefreshToken() ?: return@refreshTokens null

                        try {
                            val response = client.post(ApiConfig.Endpoints.REFRESH_TOKEN) {
                                markAsRefreshTokenRequest()
                                setBody(mapOf("refreshToken" to refreshToken))
                            }

                            val authResponse = response.body<ApiResponse<com.covoyage.domain.model.AuthResponse>>()
                            if (authResponse.success && authResponse.data != null) {
                                tokenManager.saveToken(authResponse.data.token)
                                tokenManager.saveRefreshToken(authResponse.data.refreshToken)
                                BearerTokens(authResponse.data.token, authResponse.data.refreshToken)
                            } else {
                                tokenManager.clearTokens()
                                null
                            }
                        } catch (e: Exception) {
                            tokenManager.clearTokens()
                            null
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun createDefault(tokenManager: TokenManager): HttpClient {
            return HttpClientFactory(tokenManager).create()
        }
    }
}
