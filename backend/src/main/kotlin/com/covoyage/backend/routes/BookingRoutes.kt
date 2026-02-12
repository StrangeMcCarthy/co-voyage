package com.covoyage.backend.routes

import com.covoyage.backend.controllers.BookingController
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.bookingRoutes(bookingController: BookingController) {
    route("/bookings") {
        authenticate("auth-jwt") {
            post {
                bookingController.createBooking(call)
            }

            get("/{id}") {
                bookingController.getBooking(call)
            }

            get("/my") {
                bookingController.getMyBookings(call)
            }

            put("/{id}/confirm-arrival") {
                bookingController.confirmArrival(call)
            }

            put("/{id}/cancel") {
                bookingController.cancelBooking(call)
            }

            put("/{id}/accept") {
                bookingController.acceptBooking(call)
            }

            put("/{id}/reject") {
                bookingController.rejectBooking(call)
            }
        }
    }
}
