package com.example.a501_a4

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

/**
 * Holds lifecycle transition logs and exposes UI-ready state.
 */
class LifecycleTrackerViewModel : ViewModel() {

    private val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val idGenerator = AtomicLong(0L)

    private val _logs = MutableStateFlow<List<LifecycleLogEntry>>(emptyList())
    val logs: StateFlow<List<LifecycleLogEntry>> = _logs.asStateFlow()

    private val _currentEvent = MutableStateFlow<Lifecycle.Event?>(null)
    val currentEvent: StateFlow<Lifecycle.Event?> = _currentEvent.asStateFlow()

    private val _snackbarEnabled = MutableStateFlow(true)
    val snackbarEnabled: StateFlow<Boolean> = _snackbarEnabled.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<LifecycleLogEntry>(extraBufferCapacity = 1)
    val snackbarEvents = _snackbarEvents.asSharedFlow()

    fun logEvent(event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_ANY) return

        val timestamp = System.currentTimeMillis()
        val entry = LifecycleLogEntry(
            id = idGenerator.incrementAndGet(),
            event = event,
            timestampMillis = timestamp,
            formattedTimestamp = synchronized(timeFormatter) {
                timeFormatter.format(Date(timestamp))
            }
        )

        _currentEvent.value = event
        _logs.update { current ->
            listOf(entry) + current
        }

        if (_snackbarEnabled.value) {
            _snackbarEvents.tryEmit(entry)
        }
    }

    fun setSnackbarsEnabled(enabled: Boolean) {
        _snackbarEnabled.value = enabled
    }

    data class LifecycleLogEntry(
        val id: Long,
        val event: Lifecycle.Event,
        val timestampMillis: Long,
        val formattedTimestamp: String
    )
}
