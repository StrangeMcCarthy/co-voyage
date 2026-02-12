package com.covoyage.backend.services

import com.covoyage.backend.models.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class FlutterwaveService {
    
    private val dotenv = dotenv { ignoreIfMissing = true }
    private val secretKey = dotenv["FLUTTERWAVE_SECRET_KEY"] ?: ""
    private val publicKey = dotenv["FLUTTERWAVE_PUBLIC_KEY"] ?: ""
    private val platformFeePercentage = dotenv["PLATFORM_FEE_PERCENTAGE"]?.toDoubleOrNull() ?: 2.0
    private val baseUrl = "https://api.flutterwave.com/v3"
    
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    suspend fun initiatePayment(
        booking: Booking,
        user: User,
        paymentMethod: PaymentMethod,
        redirectUrl: String?
    ): InitiatePaymentResponse {
        val platformFee = calculatePlatformFee(booking.totalAmount)
        val driverAmount = booking.totalAmount - platformFee
        
        // Create payment record (would be done in Payment repository)
        val paymentId = org.bson.types.ObjectId().toHexString()
        
        val paymentRequest = FlutterwavePaymentRequest(
            tx_ref = paymentId,
            amount = booking.totalAmount,
            currency = "XAF",
            redirect_url = redirectUrl ?: "${dotenv["FRONTEND_URL"]}/payment/callback",
            payment_options = getPaymentOptions(paymentMethod),
            customer = FlutterwaveCustomer(
                email = user.email,
                phonenumber = user.phoneNumber,
                name = user.name
            ),
            customizations = FlutterwaveCustomization(
                title = "Co-Voyage Ride Payment",
                description = "Payment for ride from ${booking.pickupLocation.name}",
                logo = "https://your-logo-url.com/logo.png"
            ),
            meta = mapOf(
                "booking_id" to booking.id,
                "passenger_id" to user.id,
                "driver_id" to booking.driverId
            )
        )
        
        val response = client.post("$baseUrl/payments") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $secretKey")
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(paymentRequest)
        }
        
        val flutterwaveResponse = response.body<FlutterwavePaymentResponse>()
        
        if (flutterwaveResponse.status == "success") {
            return InitiatePaymentResponse(
                paymentId = paymentId,
                paymentUrl = flutterwaveResponse.data.link,
                reference = paymentId
            )
        } else {
            throw Exception("Failed to initiate payment with Flutterwave")
        }
    }
    
    suspend fun verifyPayment(transactionId: String): FlutterwaveVerificationResponse {
        val response = client.get("$baseUrl/transactions/$transactionId/verify") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $secretKey")
            }
        }
        
        return response.body<FlutterwaveVerificationResponse>()
    }
    
    suspend fun initiateRefund(transactionId: String, amount: Double): Boolean {
        val response = client.post("$baseUrl/transactions/$transactionId/refund") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $secretKey")
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(mapOf("amount" to amount))
        }
        
        val refundResponse = response.body<FlutterwaveRefundResponse>()
        return refundResponse.status == "success"
    }
    
    private fun calculatePlatformFee(amount: Double): Double {
        return (amount * platformFeePercentage) / 100
    }
    
    private fun getPaymentOptions(method: PaymentMethod): String {
        return when (method) {
            PaymentMethod.ORANGE_MONEY -> "mobilemoneyfranco"
            PaymentMethod.MTN_MOBILE_MONEY -> "mobilemoneyghana"
            PaymentMethod.CARD -> "card"
            PaymentMethod.BANK_TRANSFER -> "banktransfer"
        }
    }
}

// Flutterwave API models
@Serializable
data class FlutterwavePaymentRequest(
    val tx_ref: String,
    val amount: Double,
    val currency: String,
    val redirect_url: String,
    val payment_options: String,
    val customer: FlutterwaveCustomer,
    val customizations: FlutterwaveCustomization,
    val meta: Map<String, String>
)

@Serializable
data class FlutterwaveCustomer(
    val email: String,
    val phonenumber: String,
    val name: String
)

@Serializable
data class FlutterwaveCustomization(
    val title: String,
    val description: String,
    val logo: String
)

@Serializable
data class FlutterwavePaymentResponse(
    val status: String,
    val message: String,
    val data: FlutterwavePaymentData
)

@Serializable
data class FlutterwavePaymentData(
    val link: String
)

@Serializable
data class FlutterwaveVerificationResponse(
    val status: String,
    val message: String,
    val data: FlutterwaveTransactionData
)

@Serializable
data class FlutterwaveTransactionData(
    val id: Long,
    val tx_ref: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val customer: FlutterwaveCustomer
)

@Serializable
data class FlutterwaveRefundResponse(
    val status: String,
    val message: String
)
