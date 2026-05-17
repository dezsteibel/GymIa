package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.LoadSuggestion
import com.gymia.domain.model.Trend
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetLoadSuggestionUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(exerciseId: Long): LoadSuggestion {
        val fourWeeksAgo = System.currentTimeMillis() - 28L * 24 * 60 * 60 * 1000
        val allSets = repository.getLoadTrendForExercise(exerciseId).first()
        val recentSets = allSets.filter { it.date >= fourWeeksAgo }

        val sessionMaxLoads = recentSets
            .groupBy { it.sessionId }
            .entries
            .sortedBy { entry -> entry.value.minOf { it.date } }
            .map { entry -> entry.value.maxOf { it.loadKg } }

        if (sessionMaxLoads.isEmpty()) return LoadSuggestion(20.0f, Trend.STABLE, 0.0f)

        val confidence = minOf(sessionMaxLoads.size / 8.0f, 1.0f)
        val lastMax = sessionMaxLoads.last()

        if (sessionMaxLoads.size < 2) return LoadSuggestion(lastMax, Trend.STABLE, confidence)

        val previousAvg = sessionMaxLoads.dropLast(1).average().toFloat()
        val last3 = sessionMaxLoads.takeLast(3)
        val isStable = last3.size >= 3 && last3.max() - last3.min() <= last3.average() * 0.02

        return when {
            lastMax > previousAvg * 1.02f -> LoadSuggestion(lastMax + 2.5f, Trend.PROGRESSING, confidence)
            lastMax < previousAvg * 0.98f -> LoadSuggestion(lastMax * 0.95f, Trend.REGRESSING, confidence)
            isStable -> LoadSuggestion(lastMax, Trend.STABLE, confidence)
            else -> LoadSuggestion(lastMax, Trend.STABLE, confidence)
        }
    }
}
