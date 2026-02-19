package covoyage.travel.cameroon.ui.journey

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import covoyage.travel.cameroon.data.model.Journey
import covoyage.travel.cameroon.data.model.JourneyStatus
import covoyage.travel.cameroon.data.model.SavedVehicle
import covoyage.travel.cameroon.data.repository.JourneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JourneyUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val journeys: List<Journey> = emptyList(),
    val selectedJourney: Journey? = null,
    val searchDepartureCity: String = "",
    val searchArrivalCity: String = "",
    val searchDate: String = "",
    // Create journey fields
    val departureCity: String = "",
    val arrivalCity: String = "",
    val departureDate: String = "",
    val departureTime: String = "",
    val totalSeats: String = "4",
    val pricePerSeat: String = "",
    val vehicleName: String = "",
    val vehicleModel: String = "",
    val vehiclePlateNumber: String = "",
    val additionalNotes: String = "",
    val journeyCreated: Boolean = false,
    // Save-for-reuse
    val savedVehicles: List<SavedVehicle> = emptyList(),
    val saveVehicle: Boolean = false,
)

class JourneyScreenModel(
    private val journeyRepository: JourneyRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow(JourneyUiState())
    val uiState: StateFlow<JourneyUiState> = _uiState.asStateFlow()

    init {
        loadJourneys()
    }

    fun loadJourneys() {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            journeyRepository.getJourneys().fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        journeys = list,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load journeys",
                    )
                }
            )
        }
    }

    fun selectJourney(journey: Journey) {
        _uiState.value = _uiState.value.copy(selectedJourney = journey)
    }

    // Search
    fun updateSearchDeparture(city: String) {
        _uiState.value = _uiState.value.copy(searchDepartureCity = city)
    }

    fun updateSearchArrival(city: String) {
        _uiState.value = _uiState.value.copy(searchArrivalCity = city)
    }

    fun updateSearchDate(date: String) {
        _uiState.value = _uiState.value.copy(searchDate = date)
    }

    fun searchJourneys() {
        val state = _uiState.value
        screenModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = "")
            journeyRepository.searchJourneys(
                departureCity = state.searchDepartureCity,
                arrivalCity = state.searchArrivalCity,
                date = state.searchDate,
            ).fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        journeys = list,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Search failed",
                    )
                }
            )
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchDepartureCity = "",
            searchArrivalCity = "",
            searchDate = "",
        )
        loadJourneys()
    }

    // Create journey field updates
    fun updateDepartureCity(city: String) {
        _uiState.value = _uiState.value.copy(departureCity = city, error = "")
    }
    fun updateArrivalCity(city: String) {
        _uiState.value = _uiState.value.copy(arrivalCity = city, error = "")
    }
    fun updateDepartureDate(date: String) {
        _uiState.value = _uiState.value.copy(departureDate = date, error = "")
    }
    fun updateDepartureTime(time: String) {
        _uiState.value = _uiState.value.copy(departureTime = time, error = "")
    }
    fun updateTotalSeats(seats: String) {
        _uiState.value = _uiState.value.copy(totalSeats = seats, error = "")
    }
    fun updatePricePerSeat(price: String) {
        _uiState.value = _uiState.value.copy(pricePerSeat = price, error = "")
    }
    fun updateVehicleName(name: String) {
        _uiState.value = _uiState.value.copy(vehicleName = name, error = "")
    }
    fun updateVehicleModel(model: String) {
        _uiState.value = _uiState.value.copy(vehicleModel = model, error = "")
    }
    fun updateVehiclePlateNumber(plate: String) {
        _uiState.value = _uiState.value.copy(vehiclePlateNumber = plate, error = "")
    }
    fun updateAdditionalNotes(notes: String) {
        _uiState.value = _uiState.value.copy(additionalNotes = notes, error = "")
    }
    fun toggleSaveVehicle() {
        _uiState.value = _uiState.value.copy(saveVehicle = !_uiState.value.saveVehicle)
    }
    fun setSavedVehicles(vehicles: List<SavedVehicle>) {
        _uiState.value = _uiState.value.copy(savedVehicles = vehicles)
    }
    fun selectSavedVehicle(vehicle: SavedVehicle) {
        _uiState.value = _uiState.value.copy(
            vehicleName = vehicle.name,
            vehicleModel = vehicle.model,
            vehiclePlateNumber = vehicle.plateNumber,
            totalSeats = vehicle.seats.toString(),
        )
    }

    fun createJourney(driverId: String, driverName: String, driverPhone: String) {
        val state = _uiState.value
        if (state.departureCity.isBlank() || state.arrivalCity.isBlank() ||
            state.departureDate.isBlank() || state.departureTime.isBlank() ||
            state.pricePerSeat.isBlank()
        ) {
            _uiState.value = state.copy(error = "Please fill in all required fields")
            return
        }

        val seats = state.totalSeats.toIntOrNull() ?: 4
        val price = state.pricePerSeat.toIntOrNull() ?: 0
        if (price <= 0) {
            _uiState.value = state.copy(error = "Price must be greater than 0")
            return
        }

        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            val journey = Journey(
                driverId = driverId,
                driverName = driverName,
                driverPhone = driverPhone,
                departureCity = state.departureCity,
                arrivalCity = state.arrivalCity,
                departureDate = state.departureDate,
                departureTime = state.departureTime,
                totalSeats = seats,
                availableSeats = seats,
                pricePerSeat = price,
                vehicleName = state.vehicleName,
                vehicleModel = state.vehicleModel,
                vehiclePlateNumber = state.vehiclePlateNumber,
                additionalNotes = state.additionalNotes,
                status = JourneyStatus.SCHEDULED,
            )
            journeyRepository.createJourney(journey).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        journeyCreated = true,
                        // Reset form
                        departureCity = "",
                        arrivalCity = "",
                        departureDate = "",
                        departureTime = "",
                        totalSeats = "4",
                        pricePerSeat = "",
                        vehicleName = "",
                        vehicleModel = "",
                        vehiclePlateNumber = "",
                        additionalNotes = "",
                        saveVehicle = false,
                    )
                    loadJourneys()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to create journey",
                    )
                }
            )
        }
    }

    fun resetJourneyCreated() {
        _uiState.value = _uiState.value.copy(journeyCreated = false)
    }
}
