package covoyage.server.service

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import covoyage.server.database.MongoConfig
import covoyage.server.model.*
import covoyage.server.security.SecurityUtils
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import org.bson.Document

/**
 * Payment business logic — initiate, webhook handling, escrow release, refund.
 * Persists payment documents to MongoDB and calls Flutterwave API.
 */
class PaymentService(
    private val mongoConfig: MongoConfig,
    private val flutterwaveService: FlutterwaveService,
) {
    private val payments get() = mongoConfig.database.getCollection<Document>("payments")

    /** HMAC secret for payment integrity — in production, load from a vault / env variable. */
    private val hmacSecret = System.getenv("PAYMENT_HMAC_SECRET") ?: "covoyage-payment-secret-key-change-in-prod"

    /**
     * Initiate a payment via Flutterwave.
     * Creates a payment document in PENDING state, then calls the Flutterwave charge API.
     */
    suspend fun initiatePayment(request: InitiatePaymentRequest): ApiResponse {
        val txRef = "covoyage-${request.bookingId}-${Clock.System.now().toEpochMilliseconds()}"
        val platformFee = (request.totalAmount * 0.10).toInt()
        val driverPayout = request.totalAmount - platformFee

        val paymentDoc = Document().apply {
            put("id", txRef)
            put("bookingId", request.bookingId)
            put("journeyId", request.journeyId)
            put("passengerId", request.passengerId)
            put("passengerName", request.passengerName)
            put("passengerEmail", request.passengerEmail)
            put("passengerPhone", request.passengerPhone)
            put("driverId", request.driverId)
            put("amount", request.totalAmount)
            put("platformFee", platformFee)
            put("driverPayout", driverPayout)
            put("currency", "XAF")
            put("paymentMethod", request.paymentMethod)
            put("status", "PENDING")
            put("txRef", txRef)
            put("flwRef", "")
            put("flwTransactionId", 0L)
            // Mask sensitive card data — never store raw card numbers or CVVs
            put("cardLast4", if (request.cardNumber.isNotBlank()) SecurityUtils.maskCardNumber(request.cardNumber) else "")
            // Compute integrity hash over critical fields
            val integrityData = "$txRef|${request.totalAmount}|${request.passengerId}|${Clock.System.now()}"
            put("integrityHash", SecurityUtils.computeHmac(integrityData, hmacSecret))
            put("createdAt", Clock.System.now().toString())
            put("updatedAt", Clock.System.now().toString())
            put("releasedAt", "")
        }

        payments.insertOne(paymentDoc)

        // Call Flutterwave based on payment method
        return try {
            when (request.paymentMethod) {
                "MTN_MOMO", "ORANGE_MONEY" -> {
                    val network = if (request.paymentMethod == "MTN_MOMO") "MTN" else "ORANGE"
                    val flwResponse = flutterwaveService.chargeMobileMoney(
                        txRef = txRef,
                        amount = request.totalAmount.toDouble(),
                        phoneNumber = request.passengerPhone,
                        email = request.passengerEmail,
                        fullname = request.passengerName,
                        network = network,
                    )

                    if (flwResponse.status == "success" && flwResponse.data != null) {
                        // Update with Flutterwave refs
                        updatePaymentFlwRefs(
                            txRef,
                            flwResponse.data.flwRef,
                            flwResponse.data.id,
                        )

                        ApiResponse(
                            success = true,
                            message = "Payment initiated. Check your phone to approve.",
                            data = PaymentStatusResponse(
                                paymentId = txRef,
                                status = "PENDING",
                                txRef = txRef,
                                flwRef = flwResponse.data.flwRef,
                                amount = request.totalAmount,
                            ),
                        )
                    } else {
                        updatePaymentStatus(txRef, "FAILED")
                        ApiResponse(
                            success = false,
                            message = flwResponse.message.ifBlank { "Charge failed" },
                        )
                    }
                }

                "CARD" -> {
                    val flwResponse = flutterwaveService.chargeCard(
                        txRef = txRef,
                        amount = request.totalAmount.toDouble(),
                        cardNumber = request.cardNumber,
                        cvv = request.cvv,
                        expiryMonth = request.expiryMonth,
                        expiryYear = request.expiryYear,
                        email = request.passengerEmail,
                        fullname = request.passengerName,
                        redirectUrl = "https://covoyage.travel/payment/callback",
                    )

                    if (flwResponse.status == "success" && flwResponse.data != null) {
                        updatePaymentFlwRefs(
                            txRef,
                            flwResponse.data.flwRef,
                            flwResponse.data.id,
                        )

                        ApiResponse(
                            success = true,
                            message = "Card charge initiated.",
                            data = PaymentStatusResponse(
                                paymentId = txRef,
                                status = "PENDING",
                                txRef = txRef,
                                flwRef = flwResponse.data.flwRef,
                                amount = request.totalAmount,
                            ),
                        )
                    } else {
                        updatePaymentStatus(txRef, "FAILED")
                        ApiResponse(
                            success = false,
                            message = flwResponse.message.ifBlank { "Card charge failed" },
                        )
                    }
                }

                else -> {
                    ApiResponse(success = false, message = "Unsupported payment method")
                }
            }
        } catch (e: Exception) {
            updatePaymentStatus(txRef, "FAILED")
            ApiResponse(success = false, message = "Payment error: ${e.message}")
        }
    }

    /**
     * Handle Flutterwave webhook (charge.completed).
     * On success → set payment to HELD (escrow).
     */
    suspend fun handleWebhook(payload: FlwWebhookPayload): Boolean {
        val data = payload.data ?: return false
        val txRef = data.txRef

        return if (data.status == "successful") {
            // Verify the transaction with Flutterwave
            val verification = flutterwaveService.verifyTransaction(data.id)
            if (verification.status == "success" && verification.data?.status == "successful") {
                updatePaymentStatus(txRef, "HELD")
                updatePaymentFlwRefs(txRef, data.flwRef, data.id)
                true
            } else {
                updatePaymentStatus(txRef, "FAILED")
                false
            }
        } else {
            updatePaymentStatus(txRef, "FAILED")
            false
        }
    }

    /**
     * Get payment status by payment ID (txRef).
     */
    suspend fun getPaymentStatus(paymentId: String): ApiResponse {
        val doc = payments.find(Filters.eq("id", paymentId)).firstOrNull()
            ?: return ApiResponse(success = false, message = "Payment not found")

        return ApiResponse(
            success = true,
            message = "Payment found",
            data = PaymentStatusResponse(
                paymentId = doc.getString("id") ?: "",
                status = doc.getString("status") ?: "UNKNOWN",
                txRef = doc.getString("txRef") ?: "",
                flwRef = doc.getString("flwRef") ?: "",
                amount = doc.getInteger("amount", 0),
            ),
        )
    }

    /**
     * Release escrow — pay 90% to driver after trip completion.
     * In production, this would call Flutterwave's transfer API.
     */
    suspend fun releasePayment(paymentId: String): ApiResponse {
        val doc = payments.find(Filters.eq("id", paymentId)).firstOrNull()
            ?: return ApiResponse(success = false, message = "Payment not found")

        val currentStatus = doc.getString("status")
        if (currentStatus != "HELD") {
            return ApiResponse(
                success = false,
                message = "Payment cannot be released (status: $currentStatus)",
            )
        }

        // In production: call Flutterwave transfer API to send driverPayout to driver's MoMo
        // For now, just update the status
        payments.updateOne(
            Filters.eq("id", paymentId),
            Updates.combine(
                Updates.set("status", "RELEASED"),
                Updates.set("releasedAt", Clock.System.now().toString()),
                Updates.set("updatedAt", Clock.System.now().toString()),
            ),
        )

        return ApiResponse(
            success = true,
            message = "Payment released to driver",
            data = PaymentStatusResponse(
                paymentId = paymentId,
                status = "RELEASED",
                amount = doc.getInteger("driverPayout", 0),
                message = "90% released to driver",
            ),
        )
    }

    /**
     * Refund a payment back to the passenger.
     * In production, this would call Flutterwave's refund API.
     */
    suspend fun refundPayment(paymentId: String): ApiResponse {
        val doc = payments.find(Filters.eq("id", paymentId)).firstOrNull()
            ?: return ApiResponse(success = false, message = "Payment not found")

        val currentStatus = doc.getString("status")
        if (currentStatus != "HELD") {
            return ApiResponse(
                success = false,
                message = "Payment cannot be refunded (status: $currentStatus)",
            )
        }

        payments.updateOne(
            Filters.eq("id", paymentId),
            Updates.combine(
                Updates.set("status", "REFUNDED"),
                Updates.set("updatedAt", Clock.System.now().toString()),
            ),
        )

        return ApiResponse(
            success = true,
            message = "Payment refunded to passenger",
            data = PaymentStatusResponse(
                paymentId = paymentId,
                status = "REFUNDED",
                amount = doc.getInteger("amount", 0),
            ),
        )
    }

    /**
     * Get all payments for a passenger.
     */
    suspend fun getPaymentsByPassenger(passengerId: String): List<PaymentStatusResponse> {
        return payments.find(Filters.eq("passengerId", passengerId))
            .toList()
            .map { doc ->
                PaymentStatusResponse(
                    paymentId = doc.getString("id") ?: "",
                    status = doc.getString("status") ?: "",
                    txRef = doc.getString("txRef") ?: "",
                    flwRef = doc.getString("flwRef") ?: "",
                    amount = doc.getInteger("amount", 0),
                )
            }
    }

    // ── Internal helpers ──────────────────────────

    private suspend fun updatePaymentStatus(txRef: String, status: String) {
        payments.updateOne(
            Filters.eq("id", txRef),
            Updates.combine(
                Updates.set("status", status),
                Updates.set("updatedAt", Clock.System.now().toString()),
            ),
        )
    }

    private suspend fun updatePaymentFlwRefs(txRef: String, flwRef: String, flwTransactionId: Long) {
        payments.updateOne(
            Filters.eq("id", txRef),
            Updates.combine(
                Updates.set("flwRef", flwRef),
                Updates.set("flwTransactionId", flwTransactionId),
                Updates.set("updatedAt", Clock.System.now().toString()),
            ),
        )
    }
}
