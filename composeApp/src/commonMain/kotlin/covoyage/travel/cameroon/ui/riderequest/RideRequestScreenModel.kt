package covoyage.travel.cameroon.ui.riderequest

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import covoyage.travel.cameroon.data.model.RideRequest
import covoyage.travel.cameroon.data.model.RideRequestStatus
import covoyage.travel.cameroon.data.repository.RideRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RideRequestUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val requests: List<RideRequest> = emptyList(),
    val searchQuery: String = "",
    // Create form fields
    val departureCity: String = "",
    val destinationCity: String = "",
    val travelDate: String = "",
    val seatsNeeded: String = "1",
    val message: String = "",
    val requestCreated: Boolean = false,
) {
    val filteredRequests: List<RideRequest>
        get() = if (searchQuery.isBlank()) requests
        else requests.filter { req ->
            req.departureCity.contains(searchQuery, ignoreCase = true) ||
                req.destinationCity.contains(searchQuery, ignoreCase = true) ||
                req.passengerName.contains(searchQuery, ignoreCase = true)
        }
}

class RideRequestScreenModel(
    private val rideRequestRepository: RideRequestRepository,
) : ScreenModel {

    private val _uiState = MutableStateFlow(RideRequestUiState())
    val uiState: StateFlow<RideRequestUiState> = _uiState.asStateFlow()

    init {
        loadRequests()
    }

    fun loadRequests() {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            rideRequestRepository.getOpenRequests().fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        requests = list,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load requests",
                    )
                }
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    // ── Create form field updaters ──

    fun updateDepartureCity(city: String) {
        _uiState.value = _uiState.value.copy(departureCity = city, error = "")
    }

    fun updateDestinationCity(city: String) {
        _uiState.value = _uiState.value.copy(destinationCity = city, error = "")
    }

    fun updateTravelDate(date: String) {
        _uiState.value = _uiState.value.copy(travelDate = date, error = "")
    }

    fun updateSeatsNeeded(seats: String) {
        _uiState.value = _uiState.value.copy(seatsNeeded = seats, error = "")
    }

    fun updateMessage(msg: String) {
        _uiState.value = _uiState.value.copy(message = msg, error = "")
    }

    fun createRequest(passengerId: String, passengerName: String, passengerPhone: String) {
        val state = _uiState.value
        if (state.departureCity.isBlank() || state.destinationCity.isBlank() ||
            state.travelDate.isBlank()
        ) {
            _uiState.value = state.copy(error = "Please fill in all required fields")
            return
        }

        val seats = state.seatsNeeded.toIntOrNull() ?: 1
        if (seats < 1) {
            _uiState.value = state.copy(error = "Seats must be at least 1")
            return
        }

        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            val request = RideRequest(
                passengerId = passengerId,
                passengerName = passengerName,
                passengerPhone = passengerPhone,
                departureCity = state.departureCity,
                destinationCity = state.destinationCity,
                travelDate = state.travelDate,
                seatsNeeded = seats,
                message = state.message,
                status = RideRequestStatus.OPEN,
            )
            rideRequestRepository.createRequest(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        requestCreated = true,
                        // Reset form
                        departureCity = "",
                        destinationCity = "",
                        travelDate = "",
                        seatsNeeded = "1",
                        message = "",
                    )
                    loadRequests()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to create request",
                    )
                }
            )
        }
    }

    fun closeRequest(requestId: String, passengerId: String) {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            rideRequestRepository.closeRequest(requestId, passengerId).fold(
                onSuccess = { loadRequests() },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to close request",
                    )
                }
            )
        }
    }

    fun resetRequestCreated() {
        _uiState.value = _uiState.value.copy(requestCreated = false)
    }
}
