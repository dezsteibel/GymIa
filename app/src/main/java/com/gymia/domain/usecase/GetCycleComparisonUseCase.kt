package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.CycleComparison
import com.gymia.domain.model.CycleStats
import com.gymia.domain.model.DomainSetWithExercise
import com.gymia.domain.model.DomainWorkoutPlan
import com.gymia.domain.model.ExerciseStats
import javax.inject.Inject

class GetCycleComparisonUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(): CycleComparison? {
        val plans = repository.getAiGeneratedPlans().take(MAX_PLANS_COMPARED)
        if (plans.isEmpty()) return null
        val current = buildCycleStats(plans[0])
        val previous = if (plans.size >= MIN_PLANS_FOR_COMPARISON) buildCycleStats(plans[1]) else null
        return CycleComparison(
            currentCycle = current,
            previousCycle = previous,
            volumeDeltaPercent = computeVolumeDelta(current, previous),
            loadDeltaPercent = computeLoadDelta(current, previous),
            mostImprovedExercise = findMostImproved(current, previous),
            mostRegressedExercise = findMostRegressed(current, previous)
        )
    }

    private suspend fun buildCycleStats(plan: DomainWorkoutPlan): CycleStats {
        val sessions = repository.getSessionIdsAndDatesForPlan(plan.id)
        val allSets = sessions.flatMap { (sessionId, _) -> repository.getSetsWithNamesForSession(sessionId) }
        val exerciseStats = buildExerciseStats(allSets)
        return CycleStats(
            planName = plan.name,
            startDate = sessions.minOfOrNull { it.second } ?: plan.createdAt,
            endDate = sessions.maxOfOrNull { it.second } ?: plan.createdAt,
            totalSessions = sessions.size,
            totalVolumeKg = computeTotalVolume(allSets),
            averageLoadKg = computeAverageLoad(allSets),
            exerciseStats = exerciseStats
        )
    }

    private fun buildExerciseStats(sets: List<DomainSetWithExercise>): List<ExerciseStats> =
        sets.groupBy { it.exerciseName }.map { (name, exerciseSets) ->
            ExerciseStats(
                exerciseName = name,
                maxLoadKg = exerciseSets.maxOf { it.loadKg },
                totalVolume = exerciseSets.sumOf { (it.reps * it.loadKg).toDouble() }.toFloat(),
                sessionsLogged = exerciseSets.map { it.sessionId }.distinct().size
            )
        }

    private fun computeTotalVolume(sets: List<DomainSetWithExercise>): Float =
        sets.sumOf { (it.reps * it.loadKg).toDouble() }.toFloat()

    private fun computeAverageLoad(sets: List<DomainSetWithExercise>): Float =
        if (sets.isEmpty()) 0f else sets.map { it.loadKg }.average().toFloat()

    private fun computeVolumeDelta(current: CycleStats, previous: CycleStats?): Float {
        previous ?: return 0f
        if (previous.totalVolumeKg == 0f) return 0f
        return (current.totalVolumeKg - previous.totalVolumeKg) / previous.totalVolumeKg * PERCENT_MULTIPLIER
    }

    private fun computeLoadDelta(current: CycleStats, previous: CycleStats?): Float {
        previous ?: return 0f
        if (previous.averageLoadKg == 0f) return 0f
        return (current.averageLoadKg - previous.averageLoadKg) / previous.averageLoadKg * PERCENT_MULTIPLIER
    }

    private fun findMostImproved(current: CycleStats, previous: CycleStats?): String? {
        previous ?: return null
        return computeExerciseDeltas(current, previous).maxByOrNull { it.second }?.first
    }

    private fun findMostRegressed(current: CycleStats, previous: CycleStats?): String? {
        previous ?: return null
        return computeExerciseDeltas(current, previous).minByOrNull { it.second }?.first
    }

    private fun computeExerciseDeltas(
        current: CycleStats,
        previous: CycleStats
    ): List<Pair<String, Float>> {
        val previousMap = previous.exerciseStats.associateBy { it.exerciseName }
        return current.exerciseStats.mapNotNull { currentStats ->
            val prevStats = previousMap[currentStats.exerciseName] ?: return@mapNotNull null
            if (prevStats.maxLoadKg == 0f) return@mapNotNull null
            val delta = (currentStats.maxLoadKg - prevStats.maxLoadKg) / prevStats.maxLoadKg
            Pair(currentStats.exerciseName, delta)
        }
    }

    private companion object {
        const val MAX_PLANS_COMPARED = 2
        const val MIN_PLANS_FOR_COMPARISON = 2
        const val PERCENT_MULTIPLIER = 100f
    }
}
