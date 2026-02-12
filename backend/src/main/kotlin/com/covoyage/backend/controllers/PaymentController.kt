package com.covoyage.backend.controllers

import com.covoyage.backend.models.*
import com.covoyage.backend.services.BookingRepository
import com.covoyage.backend.services.FlutterwaveService
import com.covoyage.backend.services.PaymentRepository
import com.covoyage.backend.utils.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.util.UUID

class PaymentController(
    private val paymentRepository: PaymentRepository,
    private val bookingRepository: BookingRepository,
    private val flutterwaveService: FlutterwaveService
) {
    private val platformFeePercentage = System.getenv("PLATFORM_FEE_PERCENTAGE")?.toDoubleOrNull() ?: 2.0

    suspend fun initiatePayment(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Unauthorized"))

            val request = call.receive<InitiatePaymentRequest>()

            val booking = bookingRepository.findById(request.bookingId)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "Booking not found"))

            if (booking.passengerId != userId) {
                return call.respond(HttpStatusCode.Forbidden,
                    ApiResponse<Nothing>(false, message = "Only the passenger can initiate payment"))
            }

            val transactionRef = "CVG-${UUID.randomUUID().toString().take(8).uppercase()}"
            val platformFee = request.amount * (platformFeePercentage / 100)
            val driverAmount = request.amount - platformFee

            val payment = Payment(
                bookingId = request.bookingId,
                userId = userId,
                amount = request.amount,
                currency = "XAF",
                method = request.method,
                status = PaymentStatus.PENDING,
                flutterwaveReference = transactionRef,
                platformFee = platformFee,
                driverAmount = driverAmount
            )

            val createdPayment = paymentRepository.create(payment)

            // Initiate Flutterwave payment
            val redirectUrl = request.redirectUrl
                ?: "${System.getenv("FRONTEND_URL") ?: "http://localhost:3000"}/payment/callback"

            val flutterwaveResponse = flutterwaveService.initiatePayment(
                FlutterwavePaymentRequest(
                    txRef = transactionRef,
                    amount = request.amount,
                    currency = "XAF",
                    redirectUrl = redirectUrl,
                    paymentType = request.method.name.lowercase(),
                    customer = FlutterwaveCustomer(
                        email = booking.passengerEmail,
                        phoneNumber = booking.passengerPhone,
                        name = booking.passengerName
                    ),
                    customizations = FlutterwaveCustomization(
                        title = "Co-Voyage Payment",
                        description = "Payment for ride booking"
                    )
                )
            )

            if (flutterwaveResponse != null) {
                call.respond(HttpStatusCode.OK,
                    ApiResponse(true, data = InitiatePaymentResponse(
                        paymentId = createdPayment.id,
                        paymentUrl = flutterwaveResponse.data?.link ?: "",
                        reference = transactionRef
                    ), message = "Payment initiated successfully"))
            } else {
                paymentRepository.updateStatus(createdPayment.id, PaymentStatus.FAILED)
                call.respond(HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(false, message = "Failed to initiate payment with provider"))
            }

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to initiate payment: ${e.message}"))
        }
    }

    suspend fun handleWebhook(call: ApplicationCall) {
        try {
            val webhookSecret = System.getenv("FLUTTERWAVE_WEBHOOK_SECRET") ?: ""
            val hashHeader = call.request.headers["verif-hash"]

            if (hashHeader != webhookSecret) {
                return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Invalid webhook signature"))
            }

            val webhook = call.receive<FlutterwaveWebhook>()

            val payment = paymentRepository.findByTransactionRef(webhook.data.txRef)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "Payment not found"))

            val newStatus = when (webhook.data.status) {
                "successful" -> PaymentStatus.COMPLETED
                "failed" -> PaymentStatus.FAILED
                else -> PaymentStatus.PENDING
            }

            paymentRepository.updateFlutterwaveDetails(
                id = payment.id,
                flutterwaveId = webhook.data.id.toString(),
                status = newStatus
            )

            if (newStatus == PaymentStatus.COMPLETED) {
                bookingRepository.updatePaymentStatus(
                    payment.bookingId,
                    "PAID",
                    payment.id
                )
            }

            call.respond(HttpStatusCode.OK, ApiResponse<Nothing>(true, message = "Webhook processed"))

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Webhook processing failed: ${e.message}"))
        }
    }

    suspend fun getPaymentStatus(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Unauthorized"))

            val paymentId = call.parameters["id"]
                ?: return call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, message = "Payment ID is required"))

            val payment = paymentRepository.findById(paymentId)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "Payment not found"))

            if (payment.userId != userId) {
                return call.respond(HttpStatusCode.Forbidden,
                    ApiResponse<Nothing>(false, message = "Access denied"))
            }

            call.respond(HttpStatusCode.OK, ApiResponse(true, data = payment))

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to get payment: ${e.message}"))
        }
    }

    suspend fun requestRefund(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Unauthorized"))

            val paymentId = call.parameters["id"]
                ?: return call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, message = "Payment ID is required"))

            val payment = paymentRepository.findById(paymentId)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "Payment not found"))

            if (payment.userId != userId) {
                return call.respond(HttpStatusCode.Forbidden,
                    ApiResponse<Nothing>(false, message = "Access denied"))
            }

            if (payment.status != PaymentStatus.COMPLETED) {
                return call.respond(HttpStatusCode.Conflict,
                    ApiResponse<Nothing>(false, message = "Only completed payments can be refunded"))
            }

            val flutterwaveId = payment.flutterwaveTransactionId
            if (flutterwaveId != null) {
                val refundResult = flutterwaveService.initiateRefund(flutterwaveId.toLong(), payment.amount)
                if (refundResult) {
                    paymentRepository.updateStatus(paymentId, PaymentStatus.REFUNDED)
                    bookingRepository.updateStatus(payment.bookingId, BookingStatus.REFUNDED)
                    call.respond(HttpStatusCode.OK,
                        ApiResponse<Nothing>(true, message = "Refund initiated successfully"))
                } else {
                    call.respond(HttpStatusCode.InternalServerError,
                        ApiResponse<Nothing>(false, message = "Failed to process refund with provider"))
                }
            } else {
                call.respond(HttpStatusCode.Conflict,
                    ApiResponse<Nothing>(false, message = "No transaction ID available for refund"))
            }

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to process refund: ${e.message}"))
        }
    }

    suspend fun getPaymentHistory(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Unauthorized"))

            val payments = paymentRepository.findByUserId(userId)
            call.respond(HttpStatusCode.OK, ApiResponse(true, data = payments))

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to get payment history: ${e.message}"))
        }
    }

    suspend fun verifyPayment(call: ApplicationCall) {
        try {
            val paymentId = call.parameters["id"]
                ?: return call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, message = "Payment ID is required"))

            val payment = paymentRepository.findById(paymentId)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "Payment not found"))

            val txId = payment.flutterwaveTransactionId
            if (txId != null) {
                val verified = flutterwaveService.verifyTransaction(txId)
                if (verified) {
                    paymentRepository.updateStatus(paymentId, PaymentStatus.COMPLETED)
                    bookingRepository.updatePaymentStatus(payment.bookingId, "PAID", paymentId)
                    call.respond(HttpStatusCode.OK,
                        ApiResponse<Nothing>(true, message = "Payment verified successfully"))
                } else {
                    call.respond(HttpStatusCode.OK,
                        ApiResponse<Nothing>(false, message = "Payment verification failed"))
                }
            } else {
                call.respond(HttpStatusCode.Conflict,
                    ApiResponse<Nothing>(false, message = "No transaction to verify"))
            }

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to verify payment: ${e.message}"))
        }
    }
}
