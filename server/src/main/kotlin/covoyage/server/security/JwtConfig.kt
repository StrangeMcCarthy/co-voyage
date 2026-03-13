package covoyage.server.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

/**
 * JWT configuration for API authentication.
 * Tokens are issued on login/register and verified on protected API routes.
 */
object JwtConfig {
    private const val ISSUER = "covoyage-server"
    private const val AUDIENCE = "covoyage-api"
    private const val VALIDITY_MS = 7 * 24 * 60 * 60 * 1000L // 7 days

    private val secret: String by lazy {
        System.getenv("JWT_SECRET") ?: "covoyage-dev-secret-change-in-production"
    }

    private val algorithm: Algorithm by lazy { Algorithm.HMAC256(secret) }

    val verifier: JWTVerifier by lazy {
        JWT.require(algorithm)
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .build()
    }

    /**
     * Generate a JWT token for a user.
     */
    fun generateToken(userId: String, userType: String): String {
        return JWT.create()
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaim("userId", userId)
            .withClaim("userType", userType)
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_MS))
            .sign(algorithm)
    }
}
