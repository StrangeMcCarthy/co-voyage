package com.covoyage.backend.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.auth.jwt.*

object JwtConfig {
    private val dotenv = dotenv { ignoreIfMissing = true }
    
    val secret = dotenv["JWT_SECRET"] ?: "default-secret-change-in-production"
    val issuer = dotenv["JWT_ISSUER"] ?: "covoyage-api"
    val audience = dotenv["JWT_AUDIENCE"] ?: "covoyage-app"
    val realm = dotenv["JWT_REALM"] ?: "covoyage"
    val expiresIn = dotenv["JWT_EXPIRES_IN"]?.toLongOrNull() ?: 86400000L // 24 hours
    val refreshExpiresIn = dotenv["JWT_REFRESH_EXPIRES_IN"]?.toLongOrNull() ?: 604800000L // 7 days
    
    fun generateToken(userId: String, expirationTime: Long = expiresIn): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + expirationTime))
            .sign(Algorithm.HMAC256(secret))
    }
    
    fun generateRefreshToken(userId: String): String {
        return generateToken(userId, refreshExpiresIn)
    }
    
    fun verifyToken(token: String): JWTCredential? {
        return try {
            val verifier = JWT.require(Algorithm.HMAC256(secret))
                .withAudience(audience)
                .withIssuer(issuer)
                .build()
            
            val decodedJWT = verifier.verify(token)
            JWTCredential(decodedJWT)
        } catch (e: Exception) {
            null
        }
    }
}
