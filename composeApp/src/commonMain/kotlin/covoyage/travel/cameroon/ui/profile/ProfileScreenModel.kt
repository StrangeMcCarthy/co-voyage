package covoyage.travel.cameroon.ui.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import covoyage.travel.cameroon.data.model.UserProfile
import covoyage.travel.cameroon.data.repository.AuthRepository
import covoyage.travel.cameroon.util.InputValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val isSuccess: Boolean = false,
)

class ProfileScreenModel(
    private val authRepository: AuthRepository,
) : ScreenModel {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun updatePayoutPhoneNumber(number: String) {
        val filtered = number.filter { it.isDigit() || it == '+' }
        
        if (!InputValidator.isValidCameroonPhone(filtered)) {
            _uiState.value = _uiState.value.copy(error = "Invalid Cameroon phone number format", isSuccess = false)
            return
        }

        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "", isSuccess = false)
            val result = authRepository.updatePayoutPhoneNumber(filtered)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update number",
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = "", isSuccess = false)
    }
}
