package covoyage.server.service

import covoyage.server.config.FlutterwaveConfig
import covoyage.server.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * HTTP client for Flutterwave v3 API.
 * Handles mobile money (MTN/Orange) and card charges.
 */
class FlutterwaveService(private val config: FlutterwaveConfig) {

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = false
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.BODY
        }
    }

    /**
     * Charge via MTN Mobile Money or Orange Money.
     * Flutterwave sends a push notification to the user's phone.
     */
    suspend fun chargeMobileMoney(
        txRef: String,
        amount: Double,
        phoneNumber: String,
        email: String,
        fullname: String,
        network: String, // "MTN" or "ORANGE"
    ): FlwChargeResponse {
        val response = httpClient.post("${config.baseUrl}/charges?type=mobile_money_franco") {
            contentType(ContentType.Application.Json)
            bearerAuth(config.secretKey)
            setBody(FlwMobileMoneyRequest(
                txRef = txRef,
                amount = amount,
                phoneNumber = phoneNumber,
                email = email,
                fullname = fullname,
                network = network,
            ))
        }
        return response.body<FlwChargeResponse>()
    }

    /**
     * Charge via card (with 3DS redirect if needed).
     */
    suspend fun chargeCard(
        txRef: String,
        amount: Double,
        cardNumber: String,
        cvv: String,
        expiryMonth: String,
        expiryYear: String,
        email: String,
        fullname: String,
        redirectUrl: String,
    ): FlwChargeResponse {
        val response = httpClient.post("${config.baseUrl}/charges?type=card") {
            contentType(ContentType.Application.Json)
            bearerAuth(config.secretKey)
            setBody(FlwCardChargeRequest(
                txRef = txRef,
                amount = amount,
                cardNumber = cardNumber,
                cvv = cvv,
                expiryMonth = expiryMonth,
                expiryYear = expiryYear,
                email = email,
                fullname = fullname,
                redirectUrl = redirectUrl,
            ))
        }
        return response.body<FlwChargeResponse>()
    }

    /**
     * Verify a transaction by its Flutterwave transaction ID.
     */
    suspend fun verifyTransaction(transactionId: Long): FlwVerifyResponse {
        val response = httpClient.get("${config.baseUrl}/transactions/$transactionId/verify") {
            bearerAuth(config.secretKey)
        }
        return response.body<FlwVerifyResponse>()
    }

    /**
     * Validate webhook signature.
     * Flutterwave sends a `verif-hash` header that must match your webhook hash.
     */
    fun verifyWebhookSignature(headerHash: String?): Boolean {
        if (config.webhookHash.isBlank()) return true // Skip if not configured
        return headerHash == config.webhookHash
    }
}
