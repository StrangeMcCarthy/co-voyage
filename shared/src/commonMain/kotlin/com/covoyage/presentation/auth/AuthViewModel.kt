package com.covoyage.presentation.auth

import com.covoyage.data.remote.ApiResult
import com.covoyage.data.repository.AuthRepository
import com.covoyage.domain.model.AuthResponse
import com.covoyage.domain.model.LoginRequest
import com.covoyage.domain.model.UserRegistrationRequest
import com.covoyage.domain.model.UserRole
import com.covoyage.presentation.StatefulViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : StatefulViewModel<AuthResponse>() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        checkLoginStatus()
    }

    fun register(
        name: String,
        email: String,
        password: String,
        phoneNumber: String,
        town: String,
        role: UserRole,
        driverLicenseNumber: String? = null,
        idNumber: String? = null
    ) {
        viewModelScope.launch {
            val request = UserRegistrationRequest(
                name = name,
                email = email,
                password = password,
                phoneNumber = phoneNumber,
                town = town,
                role = role,
                driverLicenseNumber = driverLicenseNumber,
                idNumber = idNumber
            )

            authRepository.register(request).collect { result ->
                when (result) {
                    is ApiResult.Loading -> setLoading(true)
                    is ApiResult.Success -> {
                        setData(result.data)
                        _isLoggedIn.value = true
                    }
                    is ApiResult.Error -> {
                        setError(result.exception.message ?: "Registration failed")
                    }
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val request = LoginRequest(email, password)

            authRepository.login(request).collect { result ->
                when (result) {
                    is ApiResult.Loading -> setLoading(true)
                    is ApiResult.Success -> {
                        setData(result.data)
                        _isLoggedIn.value = true
                    }
                    is ApiResult.Error -> {
                        setError(result.exception.message ?: "Login failed")
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout().collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _isLoggedIn.value = false
                        updateState { copy(data = null, isLoading = false, error = null) }
                    }
                    else -> { /* Handle error if needed */ }
                }
            }
        }
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            _isLoggedIn.value = authRepository.isLoggedIn()
        }
    }
}
