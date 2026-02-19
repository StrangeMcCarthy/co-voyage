package covoyage.server.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ──────────────────────────────────────────────
// Flutterwave API request/response DTOs
// ──────────────────────────────────────────────

@Serializable
data class FlwMobileMoneyRequest(
    @SerialName("tx_ref") val txRef: String,
    val amount: Double,
    val currency: String = "XAF",
    val country: String = "CM",
    val network: String,            // "MTN" or "ORANGE"
    @SerialName("phone_number") val phoneNumber: String,
    val email: String,
    val fullname: String = "",
)

@Serializable
data class FlwCardChargeRequest(
    @SerialName("tx_ref") val txRef: String,
    val amount: Double,
    val currency: String = "XAF",
    @SerialName("card_number") val cardNumber: String,
    val cvv: String,
    @SerialName("expiry_month") val expiryMonth: String,
    @SerialName("expiry_year") val expiryYear: String,
    val email: String,
    val fullname: String = "",
    @SerialName("redirect_url") val redirectUrl: String = "",
)

@Serializable
data class FlwChargeResponse(
    val status: String = "",      // "success" or "error"
    val message: String = "",
    val data: FlwChargeData? = null,
)

@Serializable
data class FlwChargeData(
    val id: Long = 0,
    @SerialName("tx_ref") val txRef: String = "",
    @SerialName("flw_ref") val flwRef: String = "",
    val amount: Double = 0.0,
    val currency: String = "XAF",
    @SerialName("charged_amount") val chargedAmount: Double = 0.0,
    val status: String = "",       // "pending", "successful", "failed"
    @SerialName("payment_type") val paymentType: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val processor_response: String = "",
)

@Serializable
data class FlwVerifyResponse(
    val status: String = "",
    val message: String = "",
    val data: FlwVerifyData? = null,
)

@Serializable
data class FlwVerifyData(
    val id: Long = 0,
    @SerialName("tx_ref") val txRef: String = "",
    @SerialName("flw_ref") val flwRef: String = "",
    val amount: Double = 0.0,
    val currency: String = "XAF",
    @SerialName("charged_amount") val chargedAmount: Double = 0.0,
    val status: String = "",
    @SerialName("payment_type") val paymentType: String = "",
)

@Serializable
data class FlwWebhookPayload(
    val event: String = "",       // "charge.completed"
    val data: FlwWebhookData? = null,
)

@Serializable
data class FlwWebhookData(
    val id: Long = 0,
    @SerialName("tx_ref") val txRef: String = "",
    @SerialName("flw_ref") val flwRef: String = "",
    val amount: Double = 0.0,
    val currency: String = "XAF",
    val status: String = "",      // "successful" or "failed"
    @SerialName("payment_type") val paymentType: String = "",
)
