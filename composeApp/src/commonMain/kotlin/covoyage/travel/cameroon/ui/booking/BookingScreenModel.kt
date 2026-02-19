package covoyage.travel.cameroon.ui.booking

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import covoyage.travel.cameroon.data.model.*
import covoyage.travel.cameroon.data.remote.PaymentApiService
import covoyage.travel.cameroon.data.repository.BookingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BookingUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val seatsToBook: Int = 1,
    val phoneNumber: String = "",
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.MTN_MOMO,
    // Card fields
    val cardNumber: String = "",
    val cardCvv: String = "",
    val cardExpiryMonth: String = "",
    val cardExpiryYear: String = "",
    // Payment state
    val currentBooking: Booking? = null,
    val currentPayment: Payment? = null,
    val paymentId: String = "",
    val paymentStatus: String = "",
    val waitingForConfirmation: Boolean = false,
    val bookingComplete: Boolean = false,
    val myBookings: List<Booking> = emptyList(),
)

class BookingScreenModel(
    private val bookingRepository: BookingRepository,
    private val paymentApiService: PaymentApiService = PaymentApiService(),
) : ScreenModel {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    fun updateSeatsToBook(seats: Int) {
        if (seats in 1..10) {
            _uiState.value = _uiState.value.copy(seatsToBook = seats, error = "")
        }
    }

    fun updatePhoneNumber(phone: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phone, error = "")
    }

    fun updatePaymentMethod(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(selectedPaymentMethod = method, error = "")
    }

    fun updateCardNumber(value: String) {
        _uiState.value = _uiState.value.copy(cardNumber = value, error = "")
    }
    fun updateCardCvv(value: String) {
        _uiState.value = _uiState.value.copy(cardCvv = value, error = "")
    }
    fun updateCardExpiryMonth(value: String) {
        _uiState.value = _uiState.value.copy(cardExpiryMonth = value, error = "")
    }
    fun updateCardExpiryYear(value: String) {
        _uiState.value = _uiState.value.copy(cardExpiryYear = value, error = "")
    }

    fun bookSeats(journey: Journey, passenger: UserProfile) {
        val state = _uiState.value

        if (state.seatsToBook > journey.availableSeats) {
            _uiState.value = state.copy(error = "Only ${journey.availableSeats} seats available")
            return
        }

        // Validate phone for MoMo/OM
        if (state.selectedPaymentMethod != PaymentMethod.CARD && state.phoneNumber.isBlank()) {
            _uiState.value = state.copy(error = "Please enter your phone number")
            return
        }

        // Validate card fields
        if (state.selectedPaymentMethod == PaymentMethod.CARD) {
            if (state.cardNumber.isBlank() || state.cardCvv.isBlank() ||
                state.cardExpiryMonth.isBlank() || state.cardExpiryYear.isBlank()
            ) {
                _uiState.value = state.copy(error = "Please fill in all card details")
                return
            }
        }

        val totalAmount = journey.pricePerSeat * state.seatsToBook

        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")

            // 1. Create local booking record
            val booking = Booking(
                journeyId = journey.id,
                passengerId = passenger.id,
                passengerName = passenger.name,
                passengerPhone = state.phoneNumber.ifBlank { passenger.phone },
                seatsBooked = state.seatsToBook,
                totalAmount = totalAmount,
            )

            bookingRepository.createBooking(booking).fold(
                onSuccess = { createdBooking ->
                    // 2. Initiate payment via server → Flutterwave
                    val paymentRequest = InitiatePaymentRequest(
                        bookingId = createdBooking.id,
                        journeyId = journey.id,
                        passengerId = passenger.id,
                        passengerName = passenger.name,
                        passengerEmail = passenger.email,
                        passengerPhone = state.phoneNumber.ifBlank { passenger.phone },
                        driverId = journey.driverId,
                        seatsBooked = state.seatsToBook,
                        totalAmount = totalAmount,
                        paymentMethod = state.selectedPaymentMethod.name,
                        cardNumber = state.cardNumber,
                        cvv = state.cardCvv,
                        expiryMonth = state.cardExpiryMonth,
                        expiryYear = state.cardExpiryYear,
                    )

                    paymentApiService.initiatePayment(paymentRequest).fold(
                        onSuccess = { apiResponse ->
                            if (apiResponse.success && apiResponse.data != null) {
                                val payment = Payment(
                                    id = apiResponse.data.paymentId,
                                    bookingId = createdBooking.id,
                                    amount = totalAmount,
                                    txRef = apiResponse.data.txRef,
                                    flwRef = apiResponse.data.flwRef,
                                    paymentMethod = state.selectedPaymentMethod,
                                    status = PaymentStatus.PENDING,
                                )

                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    currentBooking = createdBooking,
                                    currentPayment = payment,
                                    paymentId = apiResponse.data.paymentId,
                                    paymentStatus = "PENDING",
                                    waitingForConfirmation = true,
                                )

                                // 3. Start polling for MoMo/OM (async confirmation)
                                if (state.selectedPaymentMethod != PaymentMethod.CARD) {
                                    startPollingPaymentStatus(apiResponse.data.paymentId)
                                }
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = apiResponse.message.ifBlank { "Payment failed" },
                                )
                            }
                        },
                        onFailure = { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Payment error: ${e.message}",
                            )
                        }
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Booking failed: ${e.message}",
                    )
                }
            )
        }
    }

    /**
     * Poll the server for payment status updates.
     * MoMo/OM payments are async — user approves on their phone,
     * Flutterwave sends a webhook, server updates status to HELD.
     */
    private fun startPollingPaymentStatus(paymentId: String) {
        screenModelScope.launch {
            var attempts = 0
            val maxAttempts = 60 // Poll for up to 5 minutes (every 5s)

            while (attempts < maxAttempts) {
                delay(5000) // 5 second intervals
                attempts++

                paymentApiService.getPaymentStatus(paymentId).fold(
                    onSuccess = { apiResponse ->
                        if (apiResponse.data != null) {
                            val status = apiResponse.data.status
                            _uiState.value = _uiState.value.copy(paymentStatus = status)

                            when (status) {
                                "HELD" -> {
                                    // Payment confirmed and in escrow!
                                    _uiState.value = _uiState.value.copy(
                                        waitingForConfirmation = false,
                                        bookingComplete = true,
                                        currentPayment = _uiState.value.currentPayment?.copy(
                                            status = PaymentStatus.HELD,
                                        ),
                                    )
                                    return@launch
                                }
                                "FAILED" -> {
                                    _uiState.value = _uiState.value.copy(
                                        waitingForConfirmation = false,
                                        error = "Payment was declined or failed",
                                    )
                                    return@launch
                                }
                            }
                        }
                    },
                    onFailure = { /* continue polling */ }
                )
            }

            // Timed out
            _uiState.value = _uiState.value.copy(
                waitingForConfirmation = false,
                error = "Payment confirmation timed out. Check your MoMo app.",
            )
        }
    }

    fun loadMyBookings(passengerId: String) {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            bookingRepository.getBookingsByPassenger(passengerId).fold(
                onSuccess = { bookings ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        myBookings = bookings,
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            )
        }
    }

    fun cancelBooking(bookingId: String) {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            bookingRepository.cancelBooking(bookingId).fold(
                onSuccess = {
                    val updated = _uiState.value.myBookings.map { b ->
                        if (b.id == bookingId) b.copy(status = BookingStatus.CANCELLED) else b
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        myBookings = updated,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Cancellation failed",
                    )
                }
            )
        }
    }

    fun resetBooking() {
        _uiState.value = BookingUiState()
    }
}
