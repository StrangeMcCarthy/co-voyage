package com.covoyage.backend.controllers

import com.covoyage.backend.models.*
import com.covoyage.backend.services.RideRepository
import com.covoyage.backend.services.UserRepository
import com.covoyage.backend.utils.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class RideController(
    private val rideRepository: RideRepository,
    private val userRepository: UserRepository
) {
    suspend fun createRide(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Unauthorized"))

            val user = userRepository.findById(userId)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "User not found"))

            if (user.role != UserRole.DRIVER) {
                return call.respond(HttpStatusCode.Forbidden,
                    ApiResponse<Nothing>(false, message = "Only drivers can create rides"))
            }

            val request = call.receive<CreateRideRequest>()

            val ride = Ride(
                driverId = userId,
                driverName = user.name,
                driverPhone = user.phoneNumber,
                driverEmail = user.email,
                driverLicenseId = user.driverLicenseNumber ?: "",
                driverRating = user.averageRating,
                driverProfileImageUrl = user.profileImageUrl,
                departingTown = request.departingTown,
                destination = request.destination,
                pickupLocation = request.pickupLocation,
                dropoffLocation = request.dropoffLocation,
                departureDate = request.departureDate,
                departureTime = request.departureTime,
                totalSeats = request.totalSeats,
                availableSeats = request.totalSeats,
                pricePerSeat = request.pricePerSeat,
                vehicleInfo = user.vehicleInfo ?: VehicleInfo(
                    make = "", model = "", year = 0, color = "",
                    plateNumber = "", capacity = request.totalSeats
                ),
                status = RideStatus.SCHEDULED,
                type = RideType.DRIVER_OFFER,
                allowsParcels = request.allowsParcels,
                parcelPricePerKg = request.parcelPricePerKg,
                description = request.description,
                amenities = request.amenities
            )

            val createdRide = rideRepository.create(ride)
            call.respond(HttpStatusCode.Created,
                ApiResponse(true, data = createdRide, message = "Ride created successfully"))

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to create ride: ${e.message}"))
        }
    }

    suspend fun searchRides(call: ApplicationCall) {
        try {
            val filter = RideFilter(
                departingTown = call.request.queryParameters["departingTown"],
                destination = call.request.queryParameters["destination"],
                date = call.request.queryParameters["date"],
                minSeats = call.request.queryParameters["minSeats"]?.toIntOrNull(),
                maxPrice = call.request.queryParameters["maxPrice"]?.toDoubleOrNull(),
                minRating = call.request.queryParameters["minRating"]?.toDoubleOrNull()
            )
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20

            val (rides, total) = rideRepository.search(filter, page, pageSize)

            call.respond(HttpStatusCode.OK,
                ApiResponse(true, data = RideSearchResponse(
                    rides = rides,
                    total = total.toInt(),
                    page = page,
                    pageSize = pageSize
                )))

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to search rides: ${e.message}"))
        }
    }

    suspend fun getRide(call: ApplicationCall) {
        try {
            val rideId = call.parameters["id"]
                ?: return call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, message = "Ride ID is required"))

            val ride = rideRepository.findById(rideId)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "Ride not found"))

            call.respond(HttpStatusCode.OK, ApiResponse(true, data = ride))

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to get ride: ${e.message}"))
        }
    }

    suspend fun getMyRides(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Unauthorized"))

            val rides = rideRepository.findByDriverId(userId)
            call.respond(HttpStatusCode.OK, ApiResponse(true, data = rides))

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to get rides: ${e.message}"))
        }
    }

    suspend fun updateRide(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Unauthorized"))

            val rideId = call.parameters["id"]
                ?: return call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, message = "Ride ID is required"))

            val ride = rideRepository.findById(rideId)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "Ride not found"))

            if (ride.driverId != userId) {
                return call.respond(HttpStatusCode.Forbidden,
                    ApiResponse<Nothing>(false, message = "You can only update your own rides"))
            }

            val updates = call.receive<Map<String, Any>>()
            val updated = rideRepository.update(rideId, updates)

            if (updated) {
                val updatedRide = rideRepository.findById(rideId)
                call.respond(HttpStatusCode.OK,
                    ApiResponse(true, data = updatedRide, message = "Ride updated successfully"))
            } else {
                call.respond(HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(false, message = "Failed to update ride"))
            }

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to update ride: ${e.message}"))
        }
    }

    suspend fun cancelRide(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: return call.respond(HttpStatusCode.Unauthorized,
                    ApiResponse<Nothing>(false, message = "Unauthorized"))

            val rideId = call.parameters["id"]
                ?: return call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(false, message = "Ride ID is required"))

            val ride = rideRepository.findById(rideId)
                ?: return call.respond(HttpStatusCode.NotFound,
                    ApiResponse<Nothing>(false, message = "Ride not found"))

            if (ride.driverId != userId) {
                return call.respond(HttpStatusCode.Forbidden,
                    ApiResponse<Nothing>(false, message = "You can only cancel your own rides"))
            }

            if (ride.status == RideStatus.COMPLETED || ride.status == RideStatus.CANCELLED) {
                return call.respond(HttpStatusCode.Conflict,
                    ApiResponse<Nothing>(false, message = "Ride is already ${ride.status.name.lowercase()}"))
            }

            val cancelled = rideRepository.updateStatus(rideId, RideStatus.CANCELLED)
            if (cancelled) {
                call.respond(HttpStatusCode.OK,
                    ApiResponse<Nothing>(true, message = "Ride cancelled successfully"))
            } else {
                call.respond(HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(false, message = "Failed to cancel ride"))
            }

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(false, message = "Failed to cancel ride: ${e.message}"))
        }
    }
}
