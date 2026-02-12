package com.covoyage.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel {
    
    protected val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    open fun onCleared() {
        viewModelScope.cancel()
    }
}

data class UiState<T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val error: String? = null
) {
    val isSuccess: Boolean get() = data != null && error == null
    val isError: Boolean get() = error != null
}

abstract class StatefulViewModel<T> : BaseViewModel() {
    
    private val _uiState = MutableStateFlow(UiState<T>())
    val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()
    
    protected fun updateState(update: UiState<T>.() -> UiState<T>) {
        _uiState.value = _uiState.value.update()
    }
    
    protected fun setLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }
    
    protected fun setData(data: T) {
        _uiState.value = _uiState.value.copy(
            data = data,
            isLoading = false,
            error = null
        )
    }
    
    protected fun setError(error: String) {
        _uiState.value = _uiState.value.copy(
            error = error,
            isLoading = false
        )
    }
    
    protected fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
