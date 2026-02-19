package covoyage.travel.cameroon.ui.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import covoyage.travel.cameroon.data.model.UserProfile
import covoyage.travel.cameroon.data.model.UserType
import covoyage.travel.cameroon.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val currentUser: UserProfile? = null,
    val isLoggedIn: Boolean = false,
    // Login fields
    val loginEmail: String = "",
    val loginPassword: String = "",
    // Registration fields
    val regName: String = "",
    val regEmail: String = "",
    val regPhone: String = "",
    val regPassword: String = "",
    val regConfirmPassword: String = "",
    val regUserType: UserType = UserType.PASSENGER,
    // Driver-specific registration fields
    val regDrivingPermit: String = "",
    val regGreyCard: String = "",
)

class AuthScreenModel(
    private val authRepository: AuthRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        _uiState.value = _uiState.value.copy(isLoggedIn = authRepository.isLoggedIn())
        screenModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                _uiState.value = _uiState.value.copy(
                    currentUser = user,
                    isLoggedIn = true,
                )
            }
        }
    }

    // Login field updates
    fun updateLoginEmail(email: String) {
        _uiState.value = _uiState.value.copy(loginEmail = email, error = "")
    }

    fun updateLoginPassword(password: String) {
        _uiState.value = _uiState.value.copy(loginPassword = password, error = "")
    }

    // Registration field updates
    fun updateRegName(name: String) {
        _uiState.value = _uiState.value.copy(regName = name, error = "")
    }

    fun updateRegEmail(email: String) {
        _uiState.value = _uiState.value.copy(regEmail = email, error = "")
    }

    fun updateRegPhone(phone: String) {
        _uiState.value = _uiState.value.copy(regPhone = phone, error = "")
    }

    fun updateRegPassword(password: String) {
        _uiState.value = _uiState.value.copy(regPassword = password, error = "")
    }

    fun updateRegConfirmPassword(password: String) {
        _uiState.value = _uiState.value.copy(regConfirmPassword = password, error = "")
    }

    fun updateRegUserType(userType: UserType) {
        _uiState.value = _uiState.value.copy(regUserType = userType, error = "")
    }

    fun updateRegDrivingPermit(permit: String) {
        _uiState.value = _uiState.value.copy(regDrivingPermit = permit, error = "")
    }

    fun updateRegGreyCard(greyCard: String) {
        _uiState.value = _uiState.value.copy(regGreyCard = greyCard, error = "")
    }

    fun login() {
        val state = _uiState.value
        if (state.loginEmail.isBlank() || state.loginPassword.isBlank()) {
            _uiState.value = state.copy(error = "Please fill in all fields")
            return
        }

        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            val result = authRepository.login(state.loginEmail, state.loginPassword)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = user,
                        isLoggedIn = true,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Login failed",
                    )
                }
            )
        }
    }

    fun register() {
        val state = _uiState.value

        // Validation
        if (state.regName.isBlank() || state.regEmail.isBlank() ||
            state.regPhone.isBlank() || state.regPassword.isBlank()
        ) {
            _uiState.value = state.copy(error = "Please fill in all required fields")
            return
        }
        if (state.regPassword != state.regConfirmPassword) {
            _uiState.value = state.copy(error = "Passwords do not match")
            return
        }
        if (state.regPassword.length < 6) {
            _uiState.value = state.copy(error = "Password must be at least 6 characters")
            return
        }
        if (state.regUserType == UserType.DRIVER) {
            if (state.regDrivingPermit.isBlank() || state.regGreyCard.isBlank()) {
                _uiState.value = state.copy(
                    error = "Driving permit and grey card numbers are required for drivers"
                )
                return
            }
        }

        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            val result = authRepository.register(
                name = state.regName,
                email = state.regEmail,
                phone = state.regPhone,
                password = state.regPassword,
                userType = state.regUserType,
                drivingPermitNumber = state.regDrivingPermit,
                greyCardNumber = state.regGreyCard,
            )
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = user,
                        isLoggedIn = true,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Registration failed",
                    )
                }
            )
        }
    }

    fun logout() {
        screenModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = "")
    }
}
