package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.DeloadReason
import com.gymia.domain.model.DeloadSuggestion
import com.gymia.domain.model.DomainSetWithDate
import javax.inject.Inject

private const val SIX_WEEKS_MS = 6L * 7 * 24 * 60 * 60 * 1000
private const val TWO_WEEKS_MS = 2L * 7 * 24 * 60 * 60 * 1000
private const val FOUR_WEEKS_MS = 4L * 7 * 24 * 60 * 60 * 1000
private const val HIGH_VOLUME_THRESHOLD = 1.20
private const val REGRESSION_MIN_EXERCISES = 2

class DetectDeloadNeedUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(): DeloadSuggestion {
        val now = System.currentTimeMillis()
        val cutoff = now - SIX_WEEKS_MS
        val allSets = workoutRepository.getAllSetsWithDatesOnce().filter { it.date >= cutoff }
        val exercises = workoutRepository.getAllExercisesAsDomainOnce().associateBy { it.id }
        if (allSets.isEmpty()) return DeloadSuggestion(false, DeloadReason.NONE, emptyList())

        val exerciseLoads = buildExerciseSessionLoads(allSets)
        val nameMap = exercises.mapValues { it.value.name }

        val regression = findRegressionExercises(exerciseLoads, nameMap)
        if (regression.size >= REGRESSION_MIN_EXERCISES) {
            return DeloadSuggestion(true, DeloadReason.REGRESSION, regression)
        }
        val stagnation = findStagnationExercises(exerciseLoads, nameMap)
        if (stagnation.isNotEmpty()) {
            return DeloadSuggestion(true, DeloadReason.STAGNATION, stagnation)
        }
        if (isHighVolume(allSets, now)) {
            return DeloadSuggestion(true, DeloadReason.HIGH_VOLUME, emptyList())
        }
        return DeloadSuggestion(false, DeloadReason.NONE, emptyList())
    }

    private fun buildExerciseSessionLoads(sets: List<DomainSetWithDate>): Map<Long, List<Pair<Long, Float>>> =
        sets.groupBy { it.exerciseId }.mapValues { (_, exSets) ->
            exSets.groupBy { it.sessionId }
                .mapValues { (_, s) -> s.maxOf { it.loadKg } }
                .map { (sid, load) -> Pair(exSets.first { it.sessionId == sid }.date, load) }
                .sortedBy { it.first }
        }

    private fun findRegressionExercises(
        loads: Map<Long, List<Pair<Long, Float>>>,
        names: Map<Long, String>
    ): List<String> = loads.entries.filter { (_, history) ->
        history.size >= 3 && history.last().second < history[history.size - 3].second
    }.mapNotNull { names[it.key] }

    private fun findStagnationExercises(
        loads: Map<Long, List<Pair<Long, Float>>>,
        names: Map<Long, String>
    ): List<String> = loads.entries.filter { (_, history) ->
        if (history.size < 3) false
        else history.takeLast(3).let { last -> last.all { it.second == last.first().second } }
    }.mapNotNull { names[it.key] }

    private fun isHighVolume(sets: List<DomainSetWithDate>, now: Long): Boolean {
        val recentSets = sets.filter { it.date >= now - TWO_WEEKS_MS }
        val prevSets = sets.filter { it.date in (now - FOUR_WEEKS_MS) until (now - TWO_WEEKS_MS) }
        val recentVol = recentSets.sumOf { (it.reps * it.loadKg).toDouble() }
        val prevVol = prevSets.sumOf { (it.reps * it.loadKg).toDouble() }
        return prevVol > 0 && recentVol > prevVol * HIGH_VOLUME_THRESHOLD
    }
}
