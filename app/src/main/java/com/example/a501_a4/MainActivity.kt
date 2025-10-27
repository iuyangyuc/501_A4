package com.example.a501_a4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.example.a501_a4.ui.theme._501_A4Theme
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val trackerViewModel: LifecycleTrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackerViewModel.logEvent(Lifecycle.Event.ON_CREATE)
        enableEdgeToEdge()
        setContent {
            _501_A4Theme {
                LifeTrackerApp(viewModel = trackerViewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        trackerViewModel.logEvent(Lifecycle.Event.ON_START)
    }

    override fun onResume() {
        super.onResume()
        trackerViewModel.logEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onPause() {
        trackerViewModel.logEvent(Lifecycle.Event.ON_PAUSE)
        super.onPause()
    }

    override fun onStop() {
        trackerViewModel.logEvent(Lifecycle.Event.ON_STOP)
        super.onStop()
    }

    override fun onDestroy() {
        trackerViewModel.logEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeTrackerApp(viewModel: LifecycleTrackerViewModel) {
    val logs by viewModel.logs.collectAsState()
    val currentEvent by viewModel.currentEvent.collectAsState()
    val snackbarEnabled by viewModel.snackbarEnabled.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.snackbarEvents.collectLatest { entry ->
            snackbarHostState.showSnackbar(
                message = context.getString(
                    R.string.snackbar_message,
                    entry.event.displayName(),
                    entry.formattedTimestamp
                )
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.life_tracker_title)) },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.toggle_snackbar_label),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = snackbarEnabled,
                            onCheckedChange = viewModel::setSnackbarsEnabled
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CurrentLifecycleStatus(currentEvent = currentEvent)
            if (logs.isEmpty()) {
                EmptyLogPlaceholder()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = logs,
                        key = { it.id }
                    ) { entry ->
                        LifecycleLogRow(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentLifecycleStatus(currentEvent: Lifecycle.Event?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.current_state_title),
                style = MaterialTheme.typography.titleMedium
            )
            if (currentEvent == null) {
                Text(
                    text = stringResource(id = R.string.waiting_for_events),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusDotForEvent(event = currentEvent)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = currentEvent.displayName(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(id = R.string.current_event_subtitle),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLogPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.log_placeholder_title),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(id = R.string.log_placeholder_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LifecycleLogRow(entry: LifecycleTrackerViewModel.LifecycleLogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusDotForEvent(event = entry.event)
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = entry.event.displayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = entry.formattedTimestamp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusDotForEvent(event: Lifecycle.Event) {
    StatusDot(color = colorForEvent(event))
}

@Composable
private fun StatusDot(color: Color) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(color = color)
    )
}

@Composable
private fun colorForEvent(event: Lifecycle.Event): Color = when (event) {
    Lifecycle.Event.ON_CREATE -> Color(0xFF4CAF50)
    Lifecycle.Event.ON_START -> Color(0xFF2196F3)
    Lifecycle.Event.ON_RESUME -> Color(0xFF0BCF9C)
    Lifecycle.Event.ON_PAUSE -> Color(0xFFFFB300)
    Lifecycle.Event.ON_STOP -> Color(0xFFFF7043)
    Lifecycle.Event.ON_DESTROY -> Color(0xFFF44336)
    Lifecycle.Event.ON_ANY -> MaterialTheme.colorScheme.primary
}

private fun Lifecycle.Event.displayName(): String =
    name.removePrefix("ON_")
        .lowercase(Locale.getDefault())
        .replaceFirstChar { ch ->
            if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString()
        }

@Preview(showBackground = true)
@Composable
private fun LifecycleLogRowPreview() {
    val sampleEntry = LifecycleTrackerViewModel.LifecycleLogEntry(
        id = 1L,
        event = Lifecycle.Event.ON_RESUME,
        timestampMillis = System.currentTimeMillis(),
        formattedTimestamp = "14:05:33.120"
    )
    _501_A4Theme {
        LifecycleLogRow(entry = sampleEntry)
    }
}

@Preview(showBackground = true)
@Composable
private fun CurrentLifecycleStatusPreview() {
    _501_A4Theme {
        CurrentLifecycleStatus(currentEvent = Lifecycle.Event.ON_START)
    }
}
