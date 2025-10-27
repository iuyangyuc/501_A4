package com.example.a501_a4

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class CounterUiState(
    val count: Int = 0,
    val isAutoMode: Boolean = false,
    val autoIntervalSeconds: Long = 3
)

class CounterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

    private var autoIncrementJob: Job? = null

    fun increment() {
        _uiState.update { current ->
            current.copy(count = current.count + 1)
        }
    }

    fun decrement() {
        _uiState.update { current ->
            current.copy(count = current.count - 1)
        }
    }

    fun reset() {
        _uiState.update { current ->
            current.copy(count = 0)
        }
    }

    fun toggleAutoMode() {
        val enableAuto = !_uiState.value.isAutoMode
        _uiState.update { current ->
            current.copy(isAutoMode = enableAuto)
        }
        if (enableAuto) {
            startAutoIncrement()
        } else {
            stopAutoIncrement()
        }
    }

    fun setAutoInterval(seconds: Long) {
        val normalizedSeconds = seconds.coerceAtLeast(1)
        val previousInterval = _uiState.value.autoIntervalSeconds
        _uiState.update { current ->
            current.copy(autoIntervalSeconds = normalizedSeconds)
        }
        if (_uiState.value.isAutoMode && normalizedSeconds != previousInterval) {
            restartAutoIncrement()
        }
    }

    private fun startAutoIncrement() {
        if (autoIncrementJob?.isActive == true) return
        autoIncrementJob = viewModelScope.launch {
            while (isActive) {
                delay(_uiState.value.autoIntervalSeconds * 1000)
                increment()
            }
        }
    }

    private fun restartAutoIncrement() {
        stopAutoIncrement()
        startAutoIncrement()
    }

    private fun stopAutoIncrement() {
        autoIncrementJob?.cancel()
        autoIncrementJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoIncrement()
    }
}
