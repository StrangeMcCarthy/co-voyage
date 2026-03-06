package covoyage.travel.cameroon.util

/**
 * Centralized input validation for the CoVoyage app.
 * Provides reusable validation functions for common field types.
 */
object InputValidator {

    // ── Email ──

    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    // ── Phone (Cameroon format) ──
    // Accepts: +237 6XXXXXXXX, +2376XXXXXXXX, 6XXXXXXXX, etc.

    private val CAMEROON_PHONE_REGEX = Regex(
        "^(\\+?237\\s?)?[6-9]\\d{7,8}$"
    )

    fun isValidCameroonPhone(phone: String): Boolean {
        val cleaned = phone.replace(Regex("[\\s()-]"), "")
        return CAMEROON_PHONE_REGEX.matches(cleaned)
    }

    // ── Date (YYYY-MM-DD) ──

    private val DATE_REGEX = Regex("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$")

    fun isValidDate(date: String): Boolean = DATE_REGEX.matches(date.trim())

    // ── Time (HH:MM, 24-hour) ──

    private val TIME_REGEX = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    fun isValidTime(time: String): Boolean = TIME_REGEX.matches(time.trim())

    // ── Numeric helpers ──

    fun isPositiveInt(value: String): Boolean {
        val num = value.trim().toIntOrNull()
        return num != null && num > 0
    }

    /** Filters a string to only allow digits */
    fun digitsOnly(value: String): String = value.filter { it.isDigit() }

    // ── Document Validation ──

    /**
     * Validates a Vehicle Identification Number (VIN)
     * Must be exactly 17 alphanumeric characters, excluding I, O, and Q.
     */
    fun isValidVIN(vin: String): Boolean {
        val vinRegex = "^[A-HJ-NPR-Z0-9]{17}$".toRegex()
        return vin.uppercase().matches(vinRegex)
    }

    /**
     * Validates a Cameroon Driving Permit
     * Pattern: 2 letters (Region) - 6 digits - 2 digits (Year)
     * e.g., CE-123456-23 or CE12345623
     */
    fun isValidCameroonPermit(permitNumber: String): Boolean {
        // Accept both format with hyphens and without hyphens
        val regexPattern = "^[A-Z]{2}-?[0-9]{6}-?[0-9]{2}$".toRegex()
        return permitNumber.uppercase().matches(regexPattern)
    }
}
