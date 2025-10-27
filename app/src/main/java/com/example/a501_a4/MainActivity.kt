package com.example.a501_a4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a501_a4.ui.theme._501_A4Theme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _501_A4Theme {
                TemperatureApp()
            }
        }
    }
}

@Composable
fun TemperatureApp(viewModel: TemperatureViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val stats = remember(uiState.readings) { calculateStats(uiState.readings) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        TemperatureDashboard(
            uiState = uiState,
            stats = stats,
            onToggle = viewModel::toggleRunning
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemperatureDashboard(
    uiState: TemperatureUiState,
    stats: TemperatureStats,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Temperature Dashboard") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                TemperatureChart(readings = uiState.readings)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Current",
                    value = formatTemperature(stats.current),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Average",
                    value = formatTemperature(stats.average),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Min",
                    value = formatTemperature(stats.min),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Max",
                    value = formatTemperature(stats.max),
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = onToggle,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(if (uiState.isRunning) "Pause Updates" else "Resume Updates")
            }

            Text(
                text = "Recent readings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (uiState.readings.isEmpty()) {
                Text(
                    text = "Waiting for readings...",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.readings.asReversed(),
                        key = { it.timestamp }
                    ) { reading ->
                        ListItem(
                            headlineContent = {
                                Text(timeFormatter.format(reading.timestamp))
                            },
                            supportingContent = {
                                Text(formatTemperature(reading.value))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TemperaturePreview() {
    val now = Instant.now()
    val sampleState = TemperatureUiState(
        readings = listOf(
            TemperatureReading(timestamp = now.minusSeconds(40), value = 67.4f),
            TemperatureReading(timestamp = now.minusSeconds(20), value = 72.9f),
            TemperatureReading(timestamp = now, value = 74.1f)
        ),
        isRunning = true
    )
    _501_A4Theme {
        TemperatureDashboard(
            uiState = sampleState,
            stats = calculateStats(sampleState.readings),
            onToggle = {}
        )
    }
}

data class TemperatureStats(
    val current: Float?,
    val average: Float?,
    val min: Float?,
    val max: Float?
)

private fun calculateStats(readings: List<TemperatureReading>): TemperatureStats {
    if (readings.isEmpty()) return TemperatureStats(null, null, null, null)
    val values = readings.map { it.value }
    val current = values.last()
    val min = values.minOrNull()
    val max = values.maxOrNull()
    val average = values.sum() / values.count()
    return TemperatureStats(current, average, min, max)
}

private fun formatTemperature(value: Float?): String {
    return value?.let { String.format("%.1f F", it) } ?: "--"
}

private fun formatTemperature(value: Float): String {
    return String.format("%.1f F", value)
}

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TemperatureChart(readings: List<TemperatureReading>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val guidelineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        if (readings.isEmpty()) {
            return@Canvas
        }

        if (readings.size == 1) {
            drawCircle(
                color = primaryColor,
                radius = 6.dp.toPx(),
                center = center
            )
            return@Canvas
        }

        val minValue = readings.minOf { it.value }
        val maxValue = readings.maxOf { it.value }
        val valueRange = (maxValue - minValue).takeIf { it > 0f } ?: 1f
        val xStep = if (readings.size > 1) size.width / readings.lastIndex else size.width
        val path = Path()

        readings.forEachIndexed { index, reading ->
            val x = index * xStep
            val normalized = (reading.value - minValue) / valueRange
            val y = size.height - normalized * size.height
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx())
        )

        val minLineY = size.height
        val maxLineY = 0f

        drawLine(
            color = guidelineColor,
            start = androidx.compose.ui.geometry.Offset(0f, minLineY),
            end = androidx.compose.ui.geometry.Offset(size.width, minLineY),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = guidelineColor,
            start = androidx.compose.ui.geometry.Offset(0f, maxLineY),
            end = androidx.compose.ui.geometry.Offset(size.width, maxLineY),
            strokeWidth = 1.dp.toPx()
        )
    }
}
