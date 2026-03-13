package covoyage.travel.cameroon.ui.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import covoyage.travel.cameroon.data.model.UserProfile
import covoyage.travel.cameroon.data.model.UserType
import covoyage.travel.cameroon.data.repository.AuthRepository
import covoyage.travel.cameroon.util.InputValidator
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
    val regPayoutPhone: String = "",
    // Forgot Password fields
    val forgotEmail: String = "",
    val resetOtp: String = "",
    val resetNewPassword: String = "",
    val resetConfirmPassword: String = "",
    val forgotPasswordStep: ForgotPasswordStep = ForgotPasswordStep.EMAIL,
    val resetSuccess: Boolean = false,
)

enum class ForgotPasswordStep {
    EMAIL, OTP, NEW_PASSWORD
}

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
        val filtered = phone.filter { it.isDigit() || it == '+' }
        _uiState.value = _uiState.value.copy(regPhone = filtered, error = "")
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

    fun updateRegPayoutPhone(phone: String) {
        val filtered = phone.filter { it.isDigit() || it == '+' }
        _uiState.value = _uiState.value.copy(regPayoutPhone = filtered, error = "")
    }

    fun login() {
        val state = _uiState.value
        val email = state.loginEmail.trim()
        val password = state.loginPassword.trim()
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = state.copy(error = "Please fill in all fields")
            return
        }

        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            val result = authRepository.login(email, password)
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
        val name = state.regName.trim()
        val email = state.regEmail.trim()
        val phone = state.regPhone.trim()
        val password = state.regPassword

        // Validation
        if (name.isBlank() || email.isBlank() ||
            phone.isBlank() || password.isBlank()
        ) {
            _uiState.value = state.copy(error = "Please fill in all required fields")
            return
        }
        if (password != state.regConfirmPassword) {
            _uiState.value = state.copy(error = "Passwords do not match")
            return
        }
        if (password.length < 6) {
            _uiState.value = state.copy(error = "Password must be at least 6 characters")
            return
        }
        if (!InputValidator.isValidEmail(email)) {
            _uiState.value = state.copy(error = "Invalid email format")
            return
        }
        if (!InputValidator.isValidCameroonPhone(phone)) {
            _uiState.value = state.copy(error = "Invalid Cameroon phone number")
            return
        }
        val finalPayoutPhone = state.regPayoutPhone.ifBlank { state.regPhone }
        
        if (state.regUserType == UserType.DRIVER) {
            if (state.regDrivingPermit.isBlank() || state.regGreyCard.isBlank()) {
                _uiState.value = state.copy(
                    error = "Driving permit and grey card numbers are required for drivers"
                )
                return
            }
            if (!InputValidator.isValidCameroonPermit(state.regDrivingPermit)) {
                _uiState.value = state.copy(
                    error = "Invalid driving permit format (e.g., CE-123456-23)"
                )
                return
            }
            if (!InputValidator.isValidVIN(state.regGreyCard)) {
                _uiState.value = state.copy(
                    error = "Invalid grey card (VIN) format (17 characters)"
                )
                return
            }
            if (!InputValidator.isValidCameroonPhone(finalPayoutPhone)) {
                _uiState.value = state.copy(error = "Invalid Cameroon payout phone number")
                return
            }
        }

        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            val result = authRepository.register(
                name = name,
                email = email,
                phone = phone,
                password = password,
                userType = state.regUserType,
                drivingPermitNumber = state.regDrivingPermit,
                greyCardNumber = state.regGreyCard,
                payoutPhoneNumber = finalPayoutPhone,
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

    // Forgot Password updates
    fun updateForgotEmail(email: String) {
        _uiState.value = _uiState.value.copy(forgotEmail = email, error = "")
    }

    fun updateResetOtp(otp: String) {
        _uiState.value = _uiState.value.copy(resetOtp = otp, error = "")
    }

    fun updateResetNewPassword(password: String) {
        _uiState.value = _uiState.value.copy(resetNewPassword = password, error = "")
    }

    fun updateResetConfirmPassword(password: String) {
        _uiState.value = _uiState.value.copy(resetConfirmPassword = password, error = "")
    }

    fun requestOtp() {
        val email = _uiState.value.forgotEmail.trim()
        if (email.isBlank() || !InputValidator.isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid email")
            return
        }

        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            val result = authRepository.forgotPassword(email)
            result.fold(
                onSuccess = { otp ->
                    // In a real app, the OTP is sent via SMS/Email. 
                    // For the mock, we can show it or just move to the next step.
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        forgotPasswordStep = ForgotPasswordStep.OTP,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to request reset code",
                    )
                }
            )
        }
    }

    fun verifyOtp() {
        val otp = _uiState.value.resetOtp
        if (otp.length != 6) {
            _uiState.value = _uiState.value.copy(error = "Please enter the 6-digit code")
            return
        }
        _uiState.value = _uiState.value.copy(forgotPasswordStep = ForgotPasswordStep.NEW_PASSWORD, error = "")
    }

    fun resetPassword() {
        val state = _uiState.value
        if (state.resetNewPassword.length < 6) {
            _uiState.value = state.copy(error = "Password must be at least 6 characters")
            return
        }
        if (state.resetNewPassword != state.resetConfirmPassword) {
            _uiState.value = state.copy(error = "Passwords do not match")
            return
        }

        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")
            val result = authRepository.resetPassword(state.forgotEmail.trim(), state.resetOtp.trim(), state.resetNewPassword)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        resetSuccess = true,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to reset password",
                    )
                }
            )
        }
    }

    fun resetForgotPasswordFlow() {
        _uiState.value = _uiState.value.copy(
            forgotEmail = "",
            resetOtp = "",
            resetNewPassword = "",
            resetConfirmPassword = "",
            forgotPasswordStep = ForgotPasswordStep.EMAIL,
            resetSuccess = false,
            error = ""
        )
    }
}
