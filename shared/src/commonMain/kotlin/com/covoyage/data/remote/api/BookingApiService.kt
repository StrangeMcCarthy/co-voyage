package com.covoyage.data.remote.api

import com.covoyage.data.remote.ApiConfig
import com.covoyage.data.remote.ApiResult
import com.covoyage.data.remote.safeApiCall
import com.covoyage.domain.model.Booking
import com.covoyage.domain.model.BookingConfirmation
import com.covoyage.domain.model.BookingHistory
import com.covoyage.domain.model.CreateBookingRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class BookingApiService(private val httpClient: HttpClient) {
    
    suspend fun createBooking(request: CreateBookingRequest): ApiResult<BookingConfirmation> = safeApiCall {
        httpClient.post(ApiConfig.Endpoints.CREATE_BOOKING) {
            setBody(request)
        }.body()
    }
    
    suspend fun getBooking(bookingId: String): ApiResult<Booking> = safeApiCall {
        httpClient.get(ApiConfig.Endpoints.BOOKING_DETAILS.replace("{id}", bookingId)).body()
    }
    
    suspend fun getMyBookings(): ApiResult<BookingHistory> = safeApiCall {
        httpClient.get(ApiConfig.Endpoints.MY_BOOKINGS).body()
    }
    
    suspend fun confirmArrival(bookingId: String): ApiResult<Booking> = safeApiCall {
        httpClient.post(ApiConfig.Endpoints.CONFIRM_ARRIVAL.replace("{id}", bookingId)).body()
    }
    
    suspend fun cancelBooking(bookingId: String, reason: String): ApiResult<Booking> = safeApiCall {
        httpClient.post(ApiConfig.Endpoints.CANCEL_BOOKING.replace("{id}", bookingId)) {
            setBody(mapOf("reason" to reason))
        }.body()
    }
    
    suspend fun getBookingsForRide(rideId: String): ApiResult<List<Booking>> = safeApiCall {
        httpClient.get("${ApiConfig.Endpoints.BOOKINGS}/ride/$rideId").body()
    }
    
    suspend fun acceptBooking(bookingId: String): ApiResult<Booking> = safeApiCall {
        httpClient.post("${ApiConfig.Endpoints.BOOKINGS}/$bookingId/accept").body()
    }
    
    suspend fun rejectBooking(bookingId: String, reason: String): ApiResult<Booking> = safeApiCall {
        httpClient.post("${ApiConfig.Endpoints.BOOKINGS}/$bookingId/reject") {
            setBody(mapOf("reason" to reason))
        }.body()
    }
}
