package com.gymia.ui.comparison

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymia.domain.model.CycleComparison
import com.gymia.domain.model.CycleStats
import com.gymia.domain.model.ExerciseStats
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val CHART_HEIGHT_DP = 220
private const val CARD_PADDING_DP = 16
private const val EXERCISE_NAME_TRUNCATE_LENGTH = 8
private val DATE_FORMAT = SimpleDateFormat("MMM dd", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleComparisonScreen(
    onBack: () -> Unit,
    viewModel: CycleComparisonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cycle Comparison") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is CycleComparisonUiState.Loading -> LoadingContent(Modifier.padding(padding))
            is CycleComparisonUiState.Empty -> EmptyContent(Modifier.padding(padding))
            is CycleComparisonUiState.Error -> ErrorContent(state.message, Modifier.padding(padding))
            is CycleComparisonUiState.Success -> ComparisonContent(
                comparison = state.comparison,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Complete your second AI cycle to see comparison",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(CARD_PADDING_DP.dp)
        )
    }
}

@Composable
private fun ErrorContent(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(CARD_PADDING_DP.dp))
    }
}

@Composable
private fun ComparisonContent(comparison: CycleComparison, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = CARD_PADDING_DP.dp),
        verticalArrangement = Arrangement.spacedBy(CARD_PADDING_DP.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        item { HeaderCard(current = comparison.currentCycle, previous = comparison.previousCycle) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CARD_PADDING_DP.dp)
            ) {
                DeltaCard(
                    label = "Volume",
                    deltaPercent = comparison.volumeDeltaPercent,
                    modifier = Modifier.weight(1f)
                )
                DeltaCard(
                    label = "Avg Load",
                    deltaPercent = comparison.loadDeltaPercent,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (comparison.previousCycle != null) {
            item {
                ExerciseBarChart(
                    currentStats = comparison.currentCycle.exerciseStats,
                    previousStats = comparison.previousCycle.exerciseStats
                )
            }
        }
        items(comparison.currentCycle.exerciseStats) { currentStats ->
            val prevStats = comparison.previousCycle?.exerciseStats
                ?.find { it.exerciseName == currentStats.exerciseName }
            ExerciseDetailRow(currentStats = currentStats, previousStats = prevStats)
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun HeaderCard(current: CycleStats, previous: CycleStats?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            Text(current.planName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "${formatDate(current.startDate)} – ${formatDate(current.endDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (previous != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "vs ${previous.planName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DeltaCard(label: String, deltaPercent: Float, modifier: Modifier = Modifier) {
    val isPositive = deltaPercent >= 0f
    val arrow = if (isPositive) "↑" else "↓"
    val color = if (isPositive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error

    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(CARD_PADDING_DP.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$arrow %.1f%%".format(kotlin.math.abs(deltaPercent)),
                style = MaterialTheme.typography.titleLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExerciseBarChart(
    currentStats: List<ExerciseStats>,
    previousStats: List<ExerciseStats>
) {
    val previousMap = previousStats.associateBy { it.exerciseName }
    val paired = currentStats.filter { previousMap.containsKey(it.exerciseName) }
    if (paired.isEmpty()) return

    val currentLoads = paired.map { it.maxLoadKg.toDouble() }
    val prevLoads = paired.map { previousMap.getValue(it.exerciseName).maxLoadKg.toDouble() }
    val labels = paired.map { it.exerciseName.take(EXERCISE_NAME_TRUNCATE_LENGTH) }

    val modelProducer = remember(paired) { CartesianChartModelProducer() }
    LaunchedEffect(paired) {
        modelProducer.runTransaction {
            columnSeries {
                series(currentLoads)
                series(prevLoads)
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            Text("Max Load per Exercise", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = CartesianValueFormatter { _, value, _ ->
                            labels.getOrNull(value.toInt()) ?: ""
                        }
                    )
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CHART_HEIGHT_DP.dp)
            )
        }
    }
}

@Composable
private fun ExerciseDetailRow(currentStats: ExerciseStats, previousStats: ExerciseStats?) {
    val deltaColor = when {
        previousStats == null -> MaterialTheme.colorScheme.onSurface
        currentStats.maxLoadKg > previousStats.maxLoadKg -> Color(0xFF4CAF50)
        currentStats.maxLoadKg < previousStats.maxLoadKg -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val deltaArrow = when {
        previousStats == null -> ""
        currentStats.maxLoadKg > previousStats.maxLoadKg -> " ↑"
        currentStats.maxLoadKg < previousStats.maxLoadKg -> " ↓"
        else -> " →"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currentStats.exerciseName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "%.1f kg".format(currentStats.maxLoadKg),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        if (previousStats != null) {
            Text(
                text = "  %.1f kg".format(previousStats.maxLoadKg),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = deltaArrow,
            style = MaterialTheme.typography.bodyMedium,
            color = deltaColor,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatDate(timestamp: Long): String = DATE_FORMAT.format(Date(timestamp))
