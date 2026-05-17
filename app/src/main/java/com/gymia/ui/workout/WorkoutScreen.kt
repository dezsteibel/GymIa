package com.gymia.ui.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymia.domain.model.DomainWorkoutDay
import com.gymia.domain.model.PlanWithDays
import com.gymia.ui.components.EmptyWorkoutPlans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = hiltViewModel(),
    onStartSession: (dayId: Long) -> Unit,
    onCreatePlan: () -> Unit,
    onEditPlan: (planId: Long) -> Unit,
    onManageExercises: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var planPendingDelete by remember { mutableStateOf<PlanWithDays?>(null) }

    planPendingDelete?.let { plan ->
        AlertDialog(
            onDismissRequest = { planPendingDelete = null },
            title = { Text("Delete Plan") },
            text = { Text("Delete \"${plan.plan.name}\" and all its days? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePlan(plan.plan.id)
                    planPendingDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { planPendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout") },
                actions = {
                    IconButton(onClick = onManageExercises) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = "Manage Exercises")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePlan) {
                Icon(Icons.Default.Add, contentDescription = "Create Plan")
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is WorkoutUiState.Loading -> LoadingContent(Modifier.padding(padding))
            is WorkoutUiState.Error -> ErrorContent(state.message, Modifier.padding(padding))
            is WorkoutUiState.Success -> PlanListContent(
                plans = state.plans,
                onStartSession = onStartSession,
                onEditPlan = onEditPlan,
                onDeletePlan = { planPendingDelete = it },
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
private fun ErrorContent(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun PlanListContent(
    plans: List<PlanWithDays>,
    onStartSession: (Long) -> Unit,
    onEditPlan: (Long) -> Unit,
    onDeletePlan: (PlanWithDays) -> Unit,
    modifier: Modifier = Modifier
) {
    if (plans.isEmpty()) {
        EmptyWorkoutPlans(modifier = modifier.fillMaxSize())
        return
    }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        items(plans, key = { it.plan.id }) { planWithDays ->
            PlanCard(
                planWithDays = planWithDays,
                onStartSession = onStartSession,
                onEdit = { onEditPlan(planWithDays.plan.id) },
                onDelete = { onDeletePlan(planWithDays) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PlanCard(
    planWithDays: PlanWithDays,
    onStartSession: (Long) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            PlanHeader(
                name = planWithDays.plan.name,
                isExpanded = isExpanded,
                onToggle = { isExpanded = !isExpanded },
                onEdit = onEdit,
                onDelete = onDelete
            )
            AnimatedVisibility(visible = isExpanded) {
                PlanDaysList(days = planWithDays.days, onStartSession = onStartSession)
            }
        }
    }
}

@Composable
private fun PlanHeader(
    name: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Plan")
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Plan", tint = MaterialTheme.colorScheme.error)
        }
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null
        )
    }
}

@Composable
private fun PlanDaysList(days: List<DomainWorkoutDay>, onStartSession: (Long) -> Unit) {
    Column {
        Spacer(Modifier.height(8.dp))
        if (days.isEmpty()) {
            Text(
                "No days configured",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            days.forEach { day -> DayRow(day = day, onStartSession = onStartSession) }
        }
    }
}

@Composable
private fun DayRow(day: DomainWorkoutDay, onStartSession: (Long) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(day.label, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = { onStartSession(day.id) }) {
            Text("Start")
        }
    }
}
