package com.covoyage.backend.controllers

import com.covoyage.backend.models.CreateRatingRequest
import com.covoyage.backend.models.Rating
import com.covoyage.backend.services.BookingRepository
import com.covoyage.backend.services.RatingRepository
import com.covoyage.backend.services.UserRepository
import com.covoyage.backend.utils.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class RatingController(
    private val ratingRepository: RatingRepository,
    private val bookingRepository: BookingRepository,
    private val userRepository: UserRepository
) {
    suspend fun createRating(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Unauthorized"))

            val request = call.receive<CreateRatingRequest>()

            // Validate rating score
            if (request.rating < 1 || request.rating > 5) {
                return call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, message = "Rating must be between 1 and 5"))
            }

            // Check booking exists and user was involved
            val booking = bookingRepository.findById(request.bookingId)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "Booking not found"))

            if (booking.passengerId != userId && booking.driverId != userId) {
                return call.respond(HttpStatusCode.Forbidden,
                    ApiResponse<Nothing>(false, message = "You can only rate users from your bookings"))
            }

            // Determine who is being rated
            val reviewedUserId = request.reviewedUserId

            // Check for duplicate rating
            if (ratingRepository.existsByBookingAndReviewer(request.bookingId, userId)) {
                return call.respond(HttpStatusCode.Conflict,
                    ApiResponse<Nothing>(false, message = "You have already rated this booking"))
            }

            val rating = Rating(
                bookingId = request.bookingId,
                reviewerId = userId,
                reviewedUserId = reviewedUserId,
                rating = request.rating,
                comment = request.comment,
                punctuality = request.punctuality,
                cleanliness = request.cleanliness,
                communication = request.communication,
                safety = request.safety,
                comfort = request.comfort
            )

            ratingRepository.create(rating)

            // Update user's average rating
            val allRatings = ratingRepository.findByReviewedUserId(reviewedUserId)
            val averageRating = allRatings.map { it.rating.toDouble() }.average()
            userRepository.updateRating(reviewedUserId, averageRating)

            call.respond(HttpStatusCode.Created,
                ApiResponse(true, data = rating, message = "Rating submitted successfully"))

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to create rating: ${e.message}"))
        }
    }

    suspend fun getUserRatings(call: ApplicationCall) {
        try {
            val userId = call.parameters["userId"]
                ?: return call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, message = "User ID is required"))

            val ratings = ratingRepository.findByReviewedUserId(userId)
            val averageRating = if (ratings.isNotEmpty()) {
                ratings.map { it.rating.toDouble() }.average()
            } else {
                0.0
            }

            call.respond(HttpStatusCode.OK,
                ApiResponse(true, data = mapOf(
                    "ratings" to ratings,
                    "averageRating" to averageRating,
                    "totalRatings" to ratings.size
                )))

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to get ratings: ${e.message}"))
        }
    }
}
