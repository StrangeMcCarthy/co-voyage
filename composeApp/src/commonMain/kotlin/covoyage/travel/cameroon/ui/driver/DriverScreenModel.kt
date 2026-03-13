package covoyage.travel.cameroon.ui.driver

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import covoyage.travel.cameroon.data.model.Journey
import covoyage.travel.cameroon.data.model.JourneyStatus
import covoyage.travel.cameroon.data.remote.DriverPayoutResponse
import covoyage.travel.cameroon.data.repository.JourneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

import covoyage.travel.cameroon.data.repository.BookingRepository
import covoyage.travel.cameroon.data.model.BookingStatus

data class DriverUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val successMessage: String = "",
    val myJourneys: List<Journey> = emptyList(),
    val payoutSummary: DriverPayoutResponse? = null,
    val showPayouts: Boolean = false,
)

class DriverScreenModel(
    private val journeyRepository: JourneyRepository,
    private val bookingRepository: BookingRepository,
) : ScreenModel {

    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    fun loadMyJourneys(driverId: String) {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            journeyRepository.getJourneysByDriver(driverId).fold(
                onSuccess = { journeys ->
                    val today = Clock.System.now().toString().take(10)
                    val sevenDaysAgo = try {
                        val currentLocalDate = LocalDate.parse(today)
                        currentLocalDate.minus(7, DateTimeUnit.DAY).toString()
                    } catch (e: Exception) {
                        ""
                    }

                    val filteredAndSorted = journeys
                        .filter { journey ->
                            // Hide completed/cancelled trips older than 7 days
                            if (journey.status == JourneyStatus.COMPLETED || journey.status == JourneyStatus.CANCELLED) {
                                journey.departureDate >= sevenDaysAgo
                            } else {
                                true
                            }
                        }
                        .sortedWith(
                            compareBy<Journey> {
                                // Priority 1: Status (SCHEDULED/IN_PROGRESS first)
                                if (it.status == JourneyStatus.SCHEDULED || it.status == JourneyStatus.IN_PROGRESS) 0 else 1
                            }.thenBy {
                                // Priority 2: Date (closest departure date at top)
                                it.departureDate
                            }.thenByDescending {
                                // Priority 3: Tie-breaker (latest created first)
                                it.createdAt
                            }
                        )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        myJourneys = filteredAndSorted,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load journeys: ${e.message}",
                    )
                },
            )
        }
    }

    fun cancelJourney(journeyId: String, driverId: String) {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            journeyRepository.updateJourneyStatus(journeyId, JourneyStatus.CANCELLED).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Journey cancelled successfully",
                    )
                    loadMyJourneys(driverId)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Cancel failed: ${e.message}",
                    )
                },
            )
        }
    }

    fun startTrip(journeyId: String, driverId: String) {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            journeyRepository.startTrip(journeyId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Trip started!",
                    )
                    loadMyJourneys(driverId)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Start failed: ${e.message}",
                    )
                },
            )
        }
    }

    fun completeTrip(journeyId: String, driverId: String) {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            
            // First, get all bookings for this journey
            val bookingsResult = bookingRepository.getBookingsByJourney(journeyId)
            if (bookingsResult.isSuccess) {
                val bookings = bookingsResult.getOrNull() ?: emptyList()
                // Mark all accepted/pending bookings as completed by driver
                bookings.filter { it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.PENDING }.forEach { booking ->
                    bookingRepository.updateBookingStatus(booking.id, BookingStatus.COMPLETED_BY_DRIVER)
                }
            }

            journeyRepository.completeTrip(journeyId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Trip completed!",
                    )
                    loadMyJourneys(driverId)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Complete failed: ${e.message}",
                    )
                },
            )
        }
    }



    fun loadPayouts(driverId: String) {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            journeyRepository.getDriverPayouts(driverId).fold(
                onSuccess = { summary ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        payoutSummary = summary,
                        showPayouts = true,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load payouts: ${e.message}",
                    )
                },
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = "", successMessage = "")
    }

    fun hidePayouts() {
        _uiState.value = _uiState.value.copy(showPayouts = false)
    }
}
