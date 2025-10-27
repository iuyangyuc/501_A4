package com.example.a501_a4

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TemperatureReading(
    val timestamp: Instant,
    val value: Float
)

data class TemperatureUiState(
    val readings: List<TemperatureReading> = emptyList(),
    val isRunning: Boolean = false
)

class TemperatureViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TemperatureUiState())
    val uiState: StateFlow<TemperatureUiState> = _uiState.asStateFlow()

    private var generatorJob: Job? = null

    init {
        resume()
    }

    fun toggleRunning() {
        if (_uiState.value.isRunning) {
            pause()
        } else {
            resume()
        }
    }

    fun resume() {
        if (generatorJob != null) return
        _uiState.update { it.copy(isRunning = true) }
        generatorJob = viewModelScope.launch {
            while (isActive) {
                emitReading()
                delay(2_000L)
            }
        }
    }

    fun pause() {
        generatorJob?.cancel()
        generatorJob = null
        _uiState.update { it.copy(isRunning = false) }
    }

    private fun emitReading() {
        val newReading = TemperatureReading(
            timestamp = Instant.now(),
            value = Random.nextDouble(65.0, 85.0).toFloat()
        )
        _uiState.update { state ->
            val updated = (state.readings + newReading).takeLast(20)
            state.copy(readings = updated)
        }
    }

    override fun onCleared() {
        generatorJob?.cancel()
        super.onCleared()
    }
}
