package com.example.a501_a4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.a501_a4.ui.theme._501_A4Theme
import kotlin.math.roundToInt

private const val CounterRoute = "counter"
private const val SettingsRoute = "settings"

class MainActivity : ComponentActivity() {

    private val counterViewModel: CounterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _501_A4Theme {
                CounterApp(counterViewModel = counterViewModel)
            }
        }
    }
}

@Composable
fun CounterApp(counterViewModel: CounterViewModel) {
    val navController = rememberNavController()
    val uiState by counterViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = CounterRoute
    ) {
        composable(CounterRoute) {
            CounterScreen(
                uiState = uiState,
                onIncrement = counterViewModel::increment,
                onDecrement = counterViewModel::decrement,
                onReset = counterViewModel::reset,
                onToggleAuto = counterViewModel::toggleAutoMode,
                onNavigateToSettings = { navController.navigate(SettingsRoute) }
            )
        }
        composable(SettingsRoute) {
            SettingsScreen(
                intervalSeconds = uiState.autoIntervalSeconds,
                onIntervalChange = counterViewModel::setAutoInterval,
                onNavigateUp = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterScreen(
    uiState: CounterUiState,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    onToggleAuto: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Counter++") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = uiState.count.toString(),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Auto mode: ${if (uiState.isAutoMode) "ON" else "OFF"}",
                style = MaterialTheme.typography.bodyLarge
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onIncrement) {
                    Text(text = "+1")
                }
                Button(onClick = onDecrement) {
                    Text(text = "-1")
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onReset) {
                    Text(text = "Reset")
                }
                FilledTonalButton(onClick = onToggleAuto) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = if (uiState.isAutoMode) "Disable Auto" else "Enable Auto")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    intervalSeconds: Long,
    onIntervalChange: (Long) -> Unit,
    onNavigateUp: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        var sliderValue by remember { mutableFloatStateOf(intervalSeconds.toFloat()) }

        LaunchedEffect(intervalSeconds) {
            sliderValue = intervalSeconds.toFloat()
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.wrapContentHeight()
            ) {
                Text(
                    text = "Auto increment interval",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${sliderValue.roundToInt()} seconds",
                    style = MaterialTheme.typography.titleLarge
                )
                Slider(
                    value = sliderValue,
                    onValueChange = { newValue ->
                        sliderValue = newValue
                    },
                    onValueChangeFinished = {
                        val seconds = sliderValue.roundToInt().coerceIn(1, 10)
                        sliderValue = seconds.toFloat()
                        onIntervalChange(seconds.toLong())
                    },
                    valueRange = 1f..10f,
                    steps = 8
                )
                Text(
                    text = "Auto mode uses this interval while running.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CounterScreenPreview() {
    _501_A4Theme {
        CounterScreen(
            uiState = CounterUiState(count = 42, isAutoMode = true),
            onIncrement = {},
            onDecrement = {},
            onReset = {},
            onToggleAuto = {},
            onNavigateToSettings = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    _501_A4Theme {
        SettingsScreen(
            intervalSeconds = 5,
            onIntervalChange = {},
            onNavigateUp = {}
        )
    }
}
