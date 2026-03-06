package covoyage.travel.cameroon.data.repository.mock

import covoyage.travel.cameroon.data.local.LocalStorageService
import covoyage.travel.cameroon.data.model.Booking
import covoyage.travel.cameroon.data.model.BookingStatus
import covoyage.travel.cameroon.data.model.Payment
import covoyage.travel.cameroon.data.model.PaymentMethod
import covoyage.travel.cameroon.data.model.PaymentStatus
import covoyage.travel.cameroon.data.repository.BookingRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MockBookingRepository(
    private val storage: LocalStorageService,
) : BookingRepository {

    private val bookings = mutableListOf<Booking>()
    private val payments = mutableListOf<Payment>()

    init {
        bookings.addAll(storage.loadList<Booking>(KEY_BOOKINGS))
        payments.addAll(storage.loadList<Payment>(KEY_PAYMENTS))
    }

    private fun persistBookings() {
        storage.saveList(KEY_BOOKINGS, bookings)
    }

    private fun persistPayments() {
        storage.saveList(KEY_PAYMENTS, payments)
    }

    override suspend fun createBooking(booking: Booking): Result<Booking> {
        val bookingId = "booking-${Uuid.random().toString().take(8)}"
        
        val newBooking = booking.copy(
            id = bookingId,
            status = BookingStatus.PENDING,
            createdAt = "2026-02-18T12:00:00Z",
        )
        bookings.add(newBooking)
        persistBookings()
        return Result.success(newBooking)
    }

    override suspend fun getBookingsByPassenger(passengerId: String): Result<List<Booking>> {
        return Result.success(bookings.filter { it.passengerId == passengerId })
    }

    override suspend fun getBookingsByJourney(journeyId: String): Result<List<Booking>> {
        return Result.success(bookings.filter { it.journeyId == journeyId })
    }

    override suspend fun cancelBooking(bookingId: String): Result<Booking> {
        return updateBookingStatusInternal(bookingId) { it.copy(status = BookingStatus.CANCELLED) }
    }

    override suspend fun updateBookingStatus(
        bookingId: String,
        status: BookingStatus
    ): Result<Booking> {
        return updateBookingStatusInternal(bookingId) { current ->
            if (status == BookingStatus.COMPLETED_BY_DRIVER) {
                // Set the timestamp so the 1-hour auto-release countdown can begin
                current.copy(
                    status = status,
                    completedAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                )
            } else {
                current.copy(status = status)
            }
        }
    }

    private fun updateBookingStatusInternal(
        bookingId: String,
        update: (Booking) -> Booking
    ): Result<Booking> {
        val index = bookings.indexOfFirst { it.id == bookingId }
        return if (index >= 0) {
            val updated = update(bookings[index])
            bookings[index] = updated
            persistBookings()
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
        persistPayments()

        // Update booking to confirmed
        val bookingIndex = bookings.indexOfFirst { it.id == booking.id }
        if (bookingIndex >= 0) {
            bookings[bookingIndex] = bookings[bookingIndex].copy(status = BookingStatus.CONFIRMED)
            persistBookings()
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
            persistPayments()
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
            persistPayments()
            Result.success(updated)
        } else {
            Result.failure(Exception("Payment not found"))
        }
    }

    companion object {
        private const val KEY_BOOKINGS = "covoyage_bookings"
        private const val KEY_PAYMENTS = "covoyage_payments"
    }
}
