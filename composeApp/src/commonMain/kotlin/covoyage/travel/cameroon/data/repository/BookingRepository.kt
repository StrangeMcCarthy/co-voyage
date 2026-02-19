package covoyage.travel.cameroon.data.repository

import covoyage.travel.cameroon.data.model.Booking
import covoyage.travel.cameroon.data.model.Payment

/**
 * Repository for booking and payment management.
 */
interface BookingRepository {
    suspend fun createBooking(booking: Booking): Result<Booking>
    suspend fun getBookingsByPassenger(passengerId: String): Result<List<Booking>>
    suspend fun getBookingsByJourney(journeyId: String): Result<List<Booking>>
    suspend fun cancelBooking(bookingId: String): Result<Booking>

    // Payment operations
    suspend fun initiatePayment(booking: Booking): Result<Payment>
    suspend fun getPaymentByBooking(bookingId: String): Result<Payment>
    suspend fun releasePayment(paymentId: String): Result<Payment>
    suspend fun refundPayment(paymentId: String): Result<Payment>
}
