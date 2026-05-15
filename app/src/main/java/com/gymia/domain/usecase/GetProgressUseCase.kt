package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.DomainExercise
import com.gymia.domain.model.DomainSetWithDate
import com.gymia.domain.model.ExerciseProgress
import com.gymia.domain.model.ProgressPoint
import com.gymia.domain.model.ProgressSummary
import com.gymia.domain.model.SessionSummary
import com.gymia.domain.model.WeeklyVolumePoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

class GetProgressUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(): Flow<ProgressSummary> =
        combine(
            repository.getAllSetsWithDates(),
            repository.getAllExercisesAsDomain(),
            repository.getSessionSummaries()
        ) { sets, exercises, sessions ->
            buildProgressSummary(sets, exercises, sessions)
        }

    private fun buildProgressSummary(
        sets: List<DomainSetWithDate>,
        exercises: List<DomainExercise>,
        sessions: List<SessionSummary>
    ): ProgressSummary {
        val exerciseMap = exercises.associateBy { it.id }
        val exerciseProgresses = buildExerciseProgresses(sets, exerciseMap)
        val weeklyVolumes = buildWeeklyVolumes(sets)
        val cycleStart = System.currentTimeMillis() - CYCLE_DURATION_MS
        val cycleSets = sets.filter { it.date >= cycleStart }
        val cycleTotalVolume = cycleSets.sumOf { (it.reps * it.loadKg).toDouble() }.toFloat()
        val cycleSessionCount = sessions.count { it.date >= cycleStart }
        return ProgressSummary(
            exerciseProgresses = exerciseProgresses,
            weeklyVolumes = weeklyVolumes,
            cycleSessionCount = cycleSessionCount,
            cycleTotalVolume = cycleTotalVolume
        )
    }

    private fun buildExerciseProgresses(
        sets: List<DomainSetWithDate>,
        exerciseMap: Map<Long, DomainExercise>
    ): List<ExerciseProgress> =
        sets.groupBy { it.exerciseId }.mapNotNull { (exerciseId, exerciseSets) ->
            val exercise = exerciseMap[exerciseId] ?: return@mapNotNull null
            val history = exerciseSets
                .groupBy { it.date }
                .map { (date, dateSets) ->
                    ProgressPoint(date = date, maxLoad = dateSets.maxOf { it.loadKg })
                }
                .sortedBy { it.date }
            val pr = history.maxByOrNull { it.maxLoad }
            ExerciseProgress(
                exerciseId = exerciseId,
                exerciseName = exercise.name,
                history = history,
                personalRecord = pr?.maxLoad ?: 0f,
                personalRecordDate = pr?.date ?: 0L
            )
        }.sortedBy { it.exerciseName }

    private fun buildWeeklyVolumes(sets: List<DomainSetWithDate>): List<WeeklyVolumePoint> =
        sets.groupBy { weekStart(it.date) }
            .map { (weekStart, weekSets) ->
                WeeklyVolumePoint(
                    weekStart = weekStart,
                    totalVolume = weekSets.sumOf { (it.reps * it.loadKg).toDouble() }.toFloat()
                )
            }
            .sortedBy { it.weekStart }

    private fun weekStart(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    companion object {
        private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000
        private const val CYCLE_DURATION_DAYS = 28L
        private const val CYCLE_DURATION_MS = CYCLE_DURATION_DAYS * MILLIS_PER_DAY
    }
}
