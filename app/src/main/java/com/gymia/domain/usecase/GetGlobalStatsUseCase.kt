package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.GlobalStats
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetGlobalStatsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(): GlobalStats {
        val sessionDates = repository.getAllSessionDatesOnce()
        val setsWithNames = repository.getAllSetsWithNamesOnce()

        val totalSessions = sessionDates.size
        val totalSetsLogged = setsWithNames.size
        val totalVolumeKg = setsWithNames.sumOf { (it.reps * it.loadKg).toDouble() }.toFloat()

        val heaviest = setsWithNames.maxByOrNull { it.loadKg }
        val heaviestSetKg = heaviest?.loadKg ?: 0f
        val heaviestSetExercise = heaviest?.exerciseName ?: "—"

        val exerciseCounts = setsWithNames.groupBy { it.exerciseName }
            .mapValues { it.value.size }
        val mostFrequentExercise = exerciseCounts.maxByOrNull { it.value }?.key ?: "—"

        val (currentStreak, longestStreak) = computeStreaks(sessionDates)

        return GlobalStats(
            totalSessions = totalSessions,
            totalVolumeKg = totalVolumeKg,
            totalSetsLogged = totalSetsLogged,
            heaviestSetKg = heaviestSetKg,
            heaviestSetExercise = heaviestSetExercise,
            mostFrequentExercise = mostFrequentExercise,
            currentStreak = currentStreak,
            longestStreak = longestStreak
        )
    }

    private fun computeStreaks(sessionDatesMs: List<Long>): Pair<Int, Int> {
        if (sessionDatesMs.isEmpty()) return Pair(0, 0)
        val daysSorted = sessionDatesMs
            .map { TimeUnit.MILLISECONDS.toDays(it) }
            .toSortedSet()
            .toList()

        var longest = 1
        var current = 1
        for (i in 1 until daysSorted.size) {
            current = if (daysSorted[i] - daysSorted[i - 1] == 1L) current + 1 else 1
            if (current > longest) longest = current
        }

        val today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        val lastDay = daysSorted.last()
        val activeStreak = if (today - lastDay <= 1) {
            var streak = 1
            for (i in daysSorted.size - 2 downTo 0) {
                if (daysSorted[i + 1] - daysSorted[i] == 1L) streak++ else break
            }
            streak
        } else 0

        return Pair(activeStreak, longest)
    }
}
