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
) : ScreenModel {

    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    fun loadMyJourneys(driverId: String) {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            journeyRepository.getJourneysByDriver(driverId).fold(
                onSuccess = { journeys ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        myJourneys = journeys.sortedByDescending { it.createdAt },
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
