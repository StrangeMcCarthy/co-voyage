package com.covoyage.backend.routes

import com.covoyage.backend.controllers.RideController
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.rideRoutes(rideController: RideController) {
    route("/rides") {
        // Public route - search rides
        get("/search") {
            rideController.searchRides(call)
        }

        // Public route - get ride details
        get("/{id}") {
            rideController.getRide(call)
        }

        // Protected routes
        authenticate("auth-jwt") {
            post {
                rideController.createRide(call)
            }

            get("/my") {
                rideController.getMyRides(call)
            }

            put("/{id}") {
                rideController.updateRide(call)
            }

            put("/{id}/cancel") {
                rideController.cancelRide(call)
            }
        }
    }
}
