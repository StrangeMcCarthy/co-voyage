package com.covoyage.backend.routes

import com.covoyage.backend.controllers.RatingController
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.ratingRoutes(ratingController: RatingController) {
    route("/ratings") {
        // Public route - get user ratings
        get("/user/{userId}") {
            ratingController.getUserRatings(call)
        }

        // Protected routes
        authenticate("auth-jwt") {
            post {
                ratingController.createRating(call)
            }
        }
    }
}
