package covoyage.server.config

/**
 * Flutterwave API configuration.
 * Secret key and webhook hash are read from environment variables.
 */
data class FlutterwaveConfig(
    val secretKey: String,
    val webhookHash: String,
    val baseUrl: String = "https://api.flutterwave.com/v3",
    val isTestMode: Boolean = true,
) {
    companion object {
        fun fromEnvironment(): FlutterwaveConfig {
            return FlutterwaveConfig(
                secretKey = System.getenv("FLW_SECRET_KEY")
                    ?: error("FLW_SECRET_KEY environment variable is required"),
                webhookHash = System.getenv("FLW_WEBHOOK_HASH") ?: "",
                isTestMode = System.getenv("FLW_TEST_MODE")?.toBooleanStrictOrNull() ?: true,
            )
        }
    }
}
