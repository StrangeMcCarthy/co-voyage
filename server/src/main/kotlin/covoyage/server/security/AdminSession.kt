package covoyage.server.security

import io.ktor.server.auth.*
import kotlinx.serialization.Serializable

@Serializable
data class AdminSession(
    val userId: String,
    val name: String,
    val email: String,
    val language: String = "EN"
) : Principal
