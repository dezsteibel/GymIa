package com.gymia.ui.templates

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymia.domain.model.TemplateDayInput
import com.gymia.domain.model.WorkoutTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    onPlanCreated: () -> Unit,
    viewModel: TemplatesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is TemplatesViewModel.UiState.PlanCreated -> onPlanCreated()
            is TemplatesViewModel.UiState.Error -> snackbarHostState.showSnackbar(state.message)
            else -> Unit
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Templates") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        TemplatesContent(
            uiState = uiState,
            modifier = Modifier.padding(padding),
            onSelectTemplate = viewModel::selectTemplate,
            onDismissDetail = viewModel::dismissDetail,
            onUseTemplate = viewModel::createPlanFromTemplate
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplatesContent(
    uiState: TemplatesViewModel.UiState,
    modifier: Modifier = Modifier,
    onSelectTemplate: (WorkoutTemplate) -> Unit,
    onDismissDetail: () -> Unit,
    onUseTemplate: (WorkoutTemplate) -> Unit
) {
    val loaded = uiState as? TemplatesViewModel.UiState.Loaded ?: return

    Box(modifier = modifier.fillMaxSize()) {
        TemplatesList(templates = loaded.templates, onSelectTemplate = onSelectTemplate)

        if (loaded.isCreating) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        loaded.selectedTemplate?.let { template ->
            TemplateDetailBottomSheet(
                template = template,
                onDismiss = onDismissDetail,
                onUseTemplate = onUseTemplate
            )
        }
    }
}

@Composable
private fun TemplatesList(
    templates: List<WorkoutTemplate>,
    onSelectTemplate: (WorkoutTemplate) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        items(templates) { template ->
            TemplateCard(template = template, onClick = { onSelectTemplate(template) })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TemplateCard(template: WorkoutTemplate, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = template.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = template.description, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = {}, label = { Text(template.targetGoal.name) })
                Spacer(modifier = Modifier.width(8.dp))
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${template.daysPerWeek} days/week",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateDetailBottomSheet(
    template: WorkoutTemplate,
    onDismiss: () -> Unit,
    onUseTemplate: (WorkoutTemplate) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        TemplateDetailContent(template = template, onUseTemplate = onUseTemplate)
    }
}

@Composable
private fun TemplateDetailContent(
    template: WorkoutTemplate,
    onUseTemplate: (WorkoutTemplate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = template.name, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = template.description, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(12.dp))
        template.days.forEach { day -> TemplateDaySection(day = day) }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onUseTemplate(template) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Use this template")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun TemplateDaySection(day: TemplateDayInput) {
    Text(text = day.label, style = MaterialTheme.typography.titleSmall)
    day.exercises.forEach { exercise ->
        Text(
            text = "• ${exercise.name} — ${exercise.setsTarget} sets",
            style = MaterialTheme.typography.bodySmall
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}
