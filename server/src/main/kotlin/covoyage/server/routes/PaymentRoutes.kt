package covoyage.server.routes

import covoyage.server.model.*
import covoyage.server.service.PaymentService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Payment REST endpoints.
 *
 * POST /api/payments/initiate     — Start a payment (MoMo, OM, or Card)
 * GET  /api/payments/{id}/status  — Poll payment status
 * POST /api/payments/webhook      — Flutterwave webhook receiver
 * POST /api/payments/{id}/release — Release escrow to driver
 * POST /api/payments/{id}/refund  — Refund to passenger
 * GET  /api/payments/passenger/{id} — List payments for a passenger
 */
fun Route.paymentRoutes(paymentService: PaymentService) {

    route("/payments") {

        /**
         * Initiate a payment.
         * Client sends booking details + payment method.
         * Server calls Flutterwave and returns payment reference.
         */
        post("/initiate") {
            val request = call.receive<InitiatePaymentRequest>()
            val response = paymentService.initiatePayment(request)

            if (response.success) {
                call.respond(HttpStatusCode.Created, response)
            } else {
                call.respond(HttpStatusCode.BadRequest, response)
            }
        }

        /**
         * Check payment status (client polls this after initiating MoMo/OM charge).
         */
        get("/{id}/status") {
            val paymentId = call.parameters["id"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(success = false, message = "Missing payment ID"),
                )

            val response = paymentService.getPaymentStatus(paymentId)
            call.respond(if (response.success) HttpStatusCode.OK else HttpStatusCode.NotFound, response)
        }

        /**
         * Flutterwave webhook endpoint.
         * Receives charge.completed events and updates payment status.
         */
        post("/webhook") {
            val payload = call.receive<FlwWebhookPayload>()

            // Verify webhook signature
            val verifyHash = call.request.header("verif-hash")
            // Note: signature verification should be done in production
            // For now we process all webhooks

            val success = paymentService.handleWebhook(payload)
            if (success) {
                call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
            } else {
                call.respond(HttpStatusCode.BadRequest, mapOf("status" to "failed"))
            }
        }

        /**
         * Release escrow to driver (called after trip completion).
         */
        post("/{id}/release") {
            val paymentId = call.parameters["id"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(success = false, message = "Missing payment ID"),
                )

            val response = paymentService.releasePayment(paymentId)
            call.respond(if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest, response)
        }

        /**
         * Refund payment to passenger.
         */
        post("/{id}/refund") {
            val paymentId = call.parameters["id"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(success = false, message = "Missing payment ID"),
                )

            val response = paymentService.refundPayment(paymentId)
            call.respond(if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest, response)
        }

        /**
         * Get all payments for a passenger.
         */
        get("/passenger/{id}") {
            val passengerId = call.parameters["id"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(success = false, message = "Missing passenger ID"),
                )

            val payments = paymentService.getPaymentsByPassenger(passengerId)
            call.respond(HttpStatusCode.OK, payments)
        }
    }
}
