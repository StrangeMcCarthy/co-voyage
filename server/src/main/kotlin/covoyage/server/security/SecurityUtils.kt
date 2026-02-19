package covoyage.server.security

import org.mindrot.jbcrypt.BCrypt
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Security utilities for CoVoyage.
 *
 * - **Passwords**: BCrypt (cost 12) — adaptive, salted, brute-force resistant.
 * - **Transaction integrity**: HMAC-SHA256 — detects tampering of payment records.
 */
object SecurityUtils {

    private const val BCRYPT_COST = 12

    // ── Password hashing (BCrypt) ──────────────────

    /** Hash a raw password with BCrypt (cost 12). */
    fun hashPassword(rawPassword: String): String =
        BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_COST))

    /** Verify a raw password against a BCrypt hash. */
    fun verifyPassword(rawPassword: String, hashedPassword: String): Boolean =
        BCrypt.checkpw(rawPassword, hashedPassword)

    // ── Transaction integrity (HMAC-SHA256) ────────

    /**
     * Compute HMAC-SHA256 over [data] using [secretKey].
     * Used to create integrity hashes for payment records.
     */
    fun computeHmac(data: String, secretKey: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(keySpec)
        return mac.doFinal(data.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    // ── Card data masking ──────────────────────────

    /** Mask a card number, keeping only the last 4 digits. */
    fun maskCardNumber(cardNumber: String): String {
        val cleaned = cardNumber.replace("\\s".toRegex(), "")
        return if (cleaned.length >= 4) "****${cleaned.takeLast(4)}" else "****"
    }
}
