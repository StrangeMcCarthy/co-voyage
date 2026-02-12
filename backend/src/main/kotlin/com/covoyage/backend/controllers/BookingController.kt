package com.covoyage.backend.controllers

import com.covoyage.backend.models.*
import com.covoyage.backend.services.BookingRepository
import com.covoyage.backend.services.RideRepository
import com.covoyage.backend.services.UserRepository
import com.covoyage.backend.utils.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class BookingController(
    private val bookingRepository: BookingRepository,
    private val rideRepository: RideRepository,
    private val userRepository: UserRepository
) {
    private val platformFeePercentage = System.getenv("PLATFORM_FEE_PERCENTAGE")?.toDoubleOrNull() ?: 2.0

    suspend fun createBooking(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Unauthorized"))

            val user = userRepository.findById(userId)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "User not found"))

            val request = call.receive<CreateBookingRequest>()

            val ride = rideRepository.findById(request.rideId)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "Ride not found"))

            if (ride.status != RideStatus.SCHEDULED) {
                return call.respond(HttpStatusCode.Conflict,
                    ApiResponse<Nothing>(false, message = "Ride is not available for booking"))
            }

            if (ride.driverId == userId) {
                return call.respond(HttpStatusCode.Conflict,
                    ApiResponse<Nothing>(false, message = "You cannot book your own ride"))
            }

            if (ride.availableSeats < request.numberOfSeats) {
                return call.respond(HttpStatusCode.Conflict,
                    ApiResponse<Nothing>(false, message = "Not enough seats available. Available: ${ride.availableSeats}"))
            }

            val seatCost = ride.pricePerSeat * request.numberOfSeats
            val parcelCost = request.parcelDetails?.parcelFee ?: 0.0
            val totalAmount = seatCost + parcelCost
            val platformFee = totalAmount * (platformFeePercentage / 100)
            val driverAmount = totalAmount - platformFee

            val booking = Booking(
                rideId = request.rideId,
                passengerId = userId,
                driverId = ride.driverId,
                passengerName = user.name,
                passengerPhone = user.phoneNumber,
                passengerEmail = user.email,
                numberOfSeats = request.numberOfSeats,
                totalAmount = totalAmount,
                platformFee = platformFee,
                driverAmount = driverAmount,
                status = BookingStatus.PENDING_PAYMENT,
                pickupLocation = request.pickupLocation,
                dropoffLocation = request.dropoffLocation,
                hasParcel = request.hasParcel,
                parcelDetails = request.parcelDetails
            )

            val createdBooking = bookingRepository.create(booking)

            // Update available seats on the ride
            rideRepository.updateAvailableSeats(request.rideId, request.numberOfSeats)

            call.respond(HttpStatusCode.Created,
                ApiResponse(true, data = createdBooking, message = "Booking created successfully"))

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to create booking: ${e.message}"))
        }
    }

    suspend fun getBooking(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Unauthorized"))

            val bookingId = call.parameters["id"]
                ?: return call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, message = "Booking ID is required"))

            val booking = bookingRepository.findById(bookingId)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "Booking not found"))

            if (booking.passengerId != userId && booking.driverId != userId) {
                return call.respond(HttpStatusCode.Forbidden,
                    ApiResponse<Nothing>(false, message = "You don't have access to this booking"))
            }

            call.respond(HttpStatusCode.OK, ApiResponse(true, data = booking))

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to get booking: ${e.message}"))
        }
    }

    suspend fun getMyBookings(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Unauthorized"))

            val user = userRepository.findById(userId)

            val bookings = if (user?.role == UserRole.DRIVER) {
                bookingRepository.findByDriverId(userId)
            } else {
                bookingRepository.findByPassengerId(userId)
            }

            call.respond(HttpStatusCode.OK, ApiResponse(true, data = bookings))

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to get bookings: ${e.message}"))
        }
    }

    suspend fun confirmArrival(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Unauthorized"))

            val bookingId = call.parameters["id"]
                ?: return call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, message = "Booking ID is required"))

            val booking = bookingRepository.findById(bookingId)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "Booking not found"))

            if (booking.passengerId != userId) {
                return call.respond(HttpStatusCode.Forbidden,
                    ApiResponse<Nothing>(false, message = "Only the passenger can confirm arrival"))
            }

            val updated = bookingRepository.updateStatus(bookingId, BookingStatus.COMPLETED)
            if (updated) {
                call.respond(HttpStatusCode.OK,
                    ApiResponse<Nothing>(true, message = "Arrival confirmed successfully"))
            } else {
                call.respond(HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(false, message = "Failed to confirm arrival"))
            }

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to confirm arrival: ${e.message}"))
        }
    }

    suspend fun cancelBooking(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Unauthorized"))

            val bookingId = call.parameters["id"]
                ?: return call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, message = "Booking ID is required"))

            val booking = bookingRepository.findById(bookingId)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "Booking not found"))

            if (booking.passengerId != userId && booking.driverId != userId) {
                return call.respond(HttpStatusCode.Forbidden,
                    ApiResponse<Nothing>(false, message = "You don't have access to this booking"))
            }

            if (booking.status == BookingStatus.COMPLETED || booking.status == BookingStatus.CANCELLED) {
                return call.respond(HttpStatusCode.Conflict,
                    ApiResponse<Nothing>(false, message = "Booking is already ${booking.status.name.lowercase()}"))
            }

            val cancelled = bookingRepository.updateStatus(bookingId, BookingStatus.CANCELLED)
            if (cancelled) {
                // Restore seats on the ride (negative seats to add back)
                rideRepository.update(booking.rideId, mapOf(
                    "availableSeats" to ((rideRepository.findById(booking.rideId)?.availableSeats ?: 0) + booking.numberOfSeats)
                ))

                call.respond(HttpStatusCode.OK,
                    ApiResponse<Nothing>(true, message = "Booking cancelled successfully"))
            } else {
                call.respond(HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(false, message = "Failed to cancel booking"))
            }

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to cancel booking: ${e.message}"))
        }
    }

    suspend fun acceptBooking(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Unauthorized"))

            val bookingId = call.parameters["id"]
                ?: return call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, message = "Booking ID is required"))

            val booking = bookingRepository.findById(bookingId)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "Booking not found"))

            if (booking.driverId != userId) {
                return call.respond(HttpStatusCode.Forbidden,
                    ApiResponse<Nothing>(false, message = "Only the driver can accept bookings"))
            }

            if (booking.status != BookingStatus.PENDING_PAYMENT) {
                return call.respond(HttpStatusCode.Conflict,
                    ApiResponse<Nothing>(false, message = "Booking is not in pending status"))
            }

            val updated = bookingRepository.updateStatus(bookingId, BookingStatus.PAYMENT_CONFIRMED)
            if (updated) {
                call.respond(HttpStatusCode.OK,
                    ApiResponse<Nothing>(true, message = "Booking accepted successfully"))
            } else {
                call.respond(HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(false, message = "Failed to accept booking"))
            }

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to accept booking: ${e.message}"))
        }
    }

    suspend fun rejectBooking(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Unauthorized"))

            val bookingId = call.parameters["id"]
                ?: return call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, message = "Booking ID is required"))

            val booking = bookingRepository.findById(bookingId)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "Booking not found"))

            if (booking.driverId != userId) {
                return call.respond(HttpStatusCode.Forbidden,
                    ApiResponse<Nothing>(false, message = "Only the driver can reject bookings"))
            }

            if (booking.status != BookingStatus.PENDING_PAYMENT) {
                return call.respond(HttpStatusCode.Conflict,
                    ApiResponse<Nothing>(false, message = "Booking is not in pending status"))
            }

            val updated = bookingRepository.updateStatus(bookingId, BookingStatus.CANCELLED)
            if (updated) {
                // Restore seats
                rideRepository.update(booking.rideId, mapOf(
                    "availableSeats" to ((rideRepository.findById(booking.rideId)?.availableSeats ?: 0) + booking.numberOfSeats)
                ))

                call.respond(HttpStatusCode.OK,
                    ApiResponse<Nothing>(true, message = "Booking rejected successfully"))
            } else {
                call.respond(HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(false, message = "Failed to reject booking"))
            }

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to reject booking: ${e.message}"))
        }
    }
}
