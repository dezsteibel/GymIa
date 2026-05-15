package com.gymia.ui.progress

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymia.domain.model.ExerciseProgress
import com.gymia.domain.model.WeeklyVolumePoint
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisTickComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val CHART_HEIGHT_DP = 200
private const val CARD_PADDING_DP = 16
private const val CHIP_SPACING_DP = 8

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: ProgressViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Progress") }) }
    ) { padding ->
        when (val state = uiState) {
            is ProgressViewModel.UiState.Loading -> LoadingContent(Modifier.padding(padding))
            is ProgressViewModel.UiState.Empty -> EmptyContent(Modifier.padding(padding))
            is ProgressViewModel.UiState.Error -> ErrorContent(state.message, Modifier.padding(padding))
            is ProgressViewModel.UiState.Success -> ProgressContent(
                state = state,
                onSelectExercise = viewModel::selectExercise,
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
        Text("No workout data yet.", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ErrorContent(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun ProgressContent(
    state: ProgressViewModel.UiState.Success,
    onSelectExercise: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(CARD_PADDING_DP.dp),
        verticalArrangement = Arrangement.spacedBy(CARD_PADDING_DP.dp)
    ) {
        item { CycleSummaryCard(state.cycleSessionCount, state.cycleTotalVolume) }
        item {
            ExerciseSelector(
                exercises = state.exerciseProgresses,
                selectedIndex = state.selectedExerciseIndex,
                onSelect = onSelectExercise
            )
        }
        item {
            val selected = state.exerciseProgresses[state.selectedExerciseIndex]
            MaxLoadChart(progress = selected)
        }
        if (state.weeklyVolumes.isNotEmpty()) {
            item { WeeklyVolumeChart(volumes = state.weeklyVolumes) }
        }
    }
}

@Composable
private fun CycleSummaryCard(sessionCount: Int, totalVolume: Float) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            Text("Last 4 Weeks", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Sessions", value = sessionCount.toString())
                StatItem(label = "Total Volume", value = "%.0f kg".format(totalVolume))
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ExerciseSelector(
    exercises: List<ExerciseProgress>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(CHIP_SPACING_DP.dp)
    ) {
        exercises.forEachIndexed { index, exercise ->
            FilterChip(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                label = { Text(exercise.exerciseName) }
            )
        }
    }
}

@Composable
private fun MaxLoadChart(progress: ExerciseProgress) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(progress.exerciseName, style = MaterialTheme.typography.titleMedium)
                if (progress.personalRecord > 0f) {
                    Text(
                        text = "PR: %.1f kg".format(progress.personalRecord),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (progress.history.size >= MIN_CHART_POINTS) {
                val modelProducer = remember(progress.exerciseId) { CartesianChartModelProducer() }
                val loads = progress.history.map { it.maxLoad.toDouble() }
                LaunchedEffect(progress.history) {
                    modelProducer.runTransaction {
                        lineSeries { series(loads) }
                    }
                }
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberLineCartesianLayer(),
                        startAxis = VerticalAxis.rememberStart(),
                        bottomAxis = HorizontalAxis.rememberBottom()
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CHART_HEIGHT_DP.dp)
                )
            } else {
                val singlePointText = progress.history.firstOrNull()
                    ?.let { "%.1f kg".format(it.maxLoad) }
                    ?: "No data"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CHART_HEIGHT_DP.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (progress.history.size == 1) "Best: $singlePointText" else "Not enough data for chart",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyVolumeChart(volumes: List<WeeklyVolumePoint>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            Text("Weekly Volume (kg)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            val modelProducer = remember { CartesianChartModelProducer() }
            val volumeValues = volumes.map { it.totalVolume.toDouble() }
            val weekLabels = volumes.map { formatWeek(it.weekStart) }
            LaunchedEffect(volumes) {
                modelProducer.runTransaction {
                    columnSeries { series(volumeValues) }
                }
            }
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = CartesianValueFormatter { _, value, _ ->
                            weekLabels.getOrNull(value.toInt()) ?: ""
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

private fun formatWeek(timestamp: Long): String =
    SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(timestamp))

private const val MIN_CHART_POINTS = 2
