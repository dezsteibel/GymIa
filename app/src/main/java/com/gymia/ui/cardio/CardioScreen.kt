package com.gymia.ui.cardio

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymia.domain.model.DomainCardioRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val CARD_PADDING_DP = 16
private const val ITEM_SPACING_DP = 8

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardioScreen(viewModel: CardioViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Cardio") }) },
        floatingActionButton = {
            if (uiState is CardioViewModel.UiState.Ready) {
                FloatingActionButton(onClick = viewModel::showForm) {
                    Icon(Icons.Default.Add, contentDescription = "Log cardio")
                }
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is CardioViewModel.UiState.Loading -> LoadingContent(Modifier.padding(padding))
            is CardioViewModel.UiState.Ready -> {
                ReadyContent(state = state, modifier = Modifier.padding(padding))
                if (state.showForm) {
                    CardioFormDialog(
                        state = state,
                        onActivityType = viewModel::updateActivityType,
                        onDuration = viewModel::updateDuration,
                        onDistance = viewModel::updateDistance,
                        onNotes = viewModel::updateNotes,
                        onSave = viewModel::saveRecord,
                        onDismiss = viewModel::hideForm
                    )
                }
            }
            is CardioViewModel.UiState.Error -> ErrorContent(state.message, Modifier.padding(padding))
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
private fun ErrorContent(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun ReadyContent(state: CardioViewModel.UiState.Ready, modifier: Modifier = Modifier) {
    if (state.history.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No cardio logged yet.", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(CARD_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(ITEM_SPACING_DP.dp)
        ) {
            items(state.history, key = { it.id }) { record ->
                CardioRecordCard(record = record)
            }
        }
    }
}

@Composable
private fun CardioRecordCard(record: DomainCardioRecord) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.activityType.replaceFirstChar { it.uppercaseChar() },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatDate(record.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            val detail = buildString {
                append("${record.durationMinutes} min")
                record.distanceKm?.let { append("  ·  %.2f km".format(it)) }
            }
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            record.notes?.let { notes ->
                Spacer(Modifier.height(4.dp))
                Text(notes, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CardioFormDialog(
    state: CardioViewModel.UiState.Ready,
    onActivityType: (String) -> Unit,
    onDuration: (String) -> Unit,
    onDistance: (String) -> Unit,
    onNotes: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Cardio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(ITEM_SPACING_DP.dp)) {
                ActivityTypeSelector(
                    selected = state.activityType,
                    onSelect = onActivityType
                )
                OutlinedTextField(
                    value = state.durationInput,
                    onValueChange = onDuration,
                    label = { Text("Duration (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.distanceInput,
                    onValueChange = onDistance,
                    label = { Text("Distance km (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.notesInput,
                    onValueChange = onNotes,
                    label = { Text("Notes (optional)") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = state.durationInput.toIntOrNull() != null && !state.isSaving
            ) {
                Text(if (state.isSaving) "Saving…" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ActivityTypeSelector(selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(ITEM_SPACING_DP.dp)) {
        CardioViewModel.ActivityType.all.forEach { type ->
            FilterChip(
                selected = type == selected,
                onClick = { onSelect(type) },
                label = { Text(type.replaceFirstChar { it.uppercaseChar() }) }
            )
        }
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
