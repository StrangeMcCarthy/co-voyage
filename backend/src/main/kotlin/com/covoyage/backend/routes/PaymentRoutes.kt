package com.covoyage.backend.routes

import com.covoyage.backend.controllers.PaymentController
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.paymentRoutes(paymentController: PaymentController) {
    route("/payments") {
        // Public webhook endpoint (verified by signature)
        post("/webhook") {
            paymentController.handleWebhook(call)
        }

        // Protected routes
        authenticate("auth-jwt") {
            post("/initiate") {
                paymentController.initiatePayment(call)
            }

            get("/{id}/status") {
                paymentController.getPaymentStatus(call)
            }

            post("/{id}/refund") {
                paymentController.requestRefund(call)
            }

            get("/history") {
                paymentController.getPaymentHistory(call)
            }

            get("/{id}/verify") {
                paymentController.verifyPayment(call)
            }
        }
    }
}
