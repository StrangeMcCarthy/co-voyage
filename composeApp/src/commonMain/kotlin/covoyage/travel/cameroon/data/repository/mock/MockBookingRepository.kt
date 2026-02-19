package covoyage.travel.cameroon.data.repository.mock

import covoyage.travel.cameroon.data.model.Booking
import covoyage.travel.cameroon.data.model.BookingStatus
import covoyage.travel.cameroon.data.model.Payment
import covoyage.travel.cameroon.data.model.PaymentMethod
import covoyage.travel.cameroon.data.model.PaymentStatus
import covoyage.travel.cameroon.data.repository.BookingRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MockBookingRepository : BookingRepository {

    private val bookings = mutableListOf<Booking>()
    private val payments = mutableListOf<Payment>()

    override suspend fun createBooking(booking: Booking): Result<Booking> {
        val newBooking = booking.copy(
            id = "booking-${Uuid.random().toString().take(8)}",
            status = BookingStatus.PENDING,
            createdAt = "2026-02-18T12:00:00Z",
        )
        bookings.add(newBooking)
        return Result.success(newBooking)
    }

    override suspend fun getBookingsByPassenger(passengerId: String): Result<List<Booking>> {
        return Result.success(bookings.filter { it.passengerId == passengerId })
    }

    override suspend fun getBookingsByJourney(journeyId: String): Result<List<Booking>> {
        return Result.success(bookings.filter { it.journeyId == journeyId })
    }

    override suspend fun cancelBooking(bookingId: String): Result<Booking> {
        val index = bookings.indexOfFirst { it.id == bookingId }
        return if (index >= 0) {
            val updated = bookings[index].copy(status = BookingStatus.CANCELLED)
            bookings[index] = updated
            Result.success(updated)
        } else {
            Result.failure(Exception("Booking not found"))
        }
    }

    override suspend fun initiatePayment(booking: Booking): Result<Payment> {
        val platformFee = (booking.totalAmount * 0.10).toInt()
        val driverPayout = booking.totalAmount - platformFee

        val payment = Payment(
            id = "payment-${Uuid.random().toString().take(8)}",
            bookingId = booking.id,
            passengerId = booking.passengerId,
            driverId = "", // will be filled from journey lookup
            amount = booking.totalAmount,
            platformFee = platformFee,
            driverPayout = driverPayout,
            paymentMethod = PaymentMethod.MTN_MOMO,
            status = PaymentStatus.HELD, // Money collected and held
            createdAt = "2026-02-18T12:00:00Z",
        )
        payments.add(payment)

        // Update booking to confirmed
        val bookingIndex = bookings.indexOfFirst { it.id == booking.id }
        if (bookingIndex >= 0) {
            bookings[bookingIndex] = bookings[bookingIndex].copy(status = BookingStatus.CONFIRMED)
        }

        return Result.success(payment)
    }

    override suspend fun getPaymentByBooking(bookingId: String): Result<Payment> {
        val payment = payments.find { it.bookingId == bookingId }
        return if (payment != null) {
            Result.success(payment)
        } else {
            Result.failure(Exception("Payment not found"))
        }
    }

    override suspend fun releasePayment(paymentId: String): Result<Payment> {
        val index = payments.indexOfFirst { it.id == paymentId }
        return if (index >= 0) {
            val updated = payments[index].copy(
                status = PaymentStatus.RELEASED,
                releasedAt = "2026-02-18T18:00:00Z",
            )
            payments[index] = updated
            Result.success(updated)
        } else {
            Result.failure(Exception("Payment not found"))
        }
    }

    override suspend fun refundPayment(paymentId: String): Result<Payment> {
        val index = payments.indexOfFirst { it.id == paymentId }
        return if (index >= 0) {
            val updated = payments[index].copy(status = PaymentStatus.REFUNDED)
            payments[index] = updated
            Result.success(updated)
        } else {
            Result.failure(Exception("Payment not found"))
        }
    }
}
