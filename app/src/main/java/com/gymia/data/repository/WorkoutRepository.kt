package com.gymia.data.repository

import com.gymia.data.local.ExerciseDao
import com.gymia.data.local.SessionDao
import com.gymia.data.local.WorkoutDao
import com.gymia.data.model.DayExercise
import com.gymia.data.model.Exercise
import com.gymia.data.model.SetRecord
import com.gymia.data.model.WorkoutDay
import com.gymia.data.model.WorkoutPlan
import com.gymia.data.model.WorkoutSession
import com.gymia.domain.model.ExerciseForDay
import com.gymia.domain.model.PlanWithDays
import com.gymia.domain.model.SessionSummary
import com.gymia.domain.model.DayInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val sessionDao: SessionDao,
    private val exerciseDao: ExerciseDao
) {
    fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAllExercises()

    fun searchExercises(query: String): Flow<List<Exercise>> = exerciseDao.search(query)

    suspend fun saveExercise(exercise: Exercise) {
        if (exercise.id == 0L) exerciseDao.insert(exercise)
        else exerciseDao.update(exercise)
    }

    suspend fun deleteExercise(exercise: Exercise) = exerciseDao.delete(exercise)

    fun getPlansWithDays(): Flow<List<PlanWithDays>> =
        workoutDao.getPlansWithDays().map { list ->
            list.map { PlanWithDays(it.plan, it.days) }
        }

    suspend fun getDayById(dayId: Long) = workoutDao.getDayById(dayId)

    suspend fun saveFullPlan(plan: WorkoutPlan, days: List<DayInput>): Long {
        val planId = workoutDao.insertPlan(plan)
        days.forEachIndexed { index, dayInput ->
            val day = WorkoutDay(planId = planId, label = dayInput.label, order = index)
            val dayId = workoutDao.insertDay(day)
            dayInput.exercises.forEachIndexed { exIndex, exInput ->
                workoutDao.insertDayExercise(
                    DayExercise(dayId = dayId, exerciseId = exInput.exerciseId, order = exIndex, setsTarget = exInput.setsTarget)
                )
            }
        }
        return planId
    }

    fun getExercisesForDay(dayId: Long): Flow<List<ExerciseForDay>> =
        workoutDao.getExercisesWithDetailForDay(dayId).map { list ->
            list.map { entity ->
                ExerciseForDay(
                    dayExerciseId = entity.dayExercise.id,
                    exercise = entity.exercise,
                    setsTarget = entity.dayExercise.setsTarget,
                    order = entity.dayExercise.order
                )
            }
        }

    fun getSessionSummaries(): Flow<List<SessionSummary>> =
        sessionDao.getSessionsWithSetCount().map { list ->
            list.map { swc ->
                SessionSummary(
                    sessionId = swc.session.id,
                    date = swc.session.date,
                    durationMinutes = swc.session.durationMinutes,
                    totalSets = swc.setCount,
                    planId = swc.session.planId,
                    dayId = swc.session.dayId,
                    planName = swc.planName,
                    notes = swc.session.notes
                )
            }
        }

    fun getSetsForExercise(exerciseId: Long): Flow<List<SetRecord>> =
        sessionDao.getSetsForExercise(exerciseId)

    suspend fun saveSession(session: WorkoutSession, sets: List<SetRecord>) {
        val sessionId = sessionDao.insertSession(session)
        sessionDao.insertSets(sets.map { it.copy(sessionId = sessionId) })
    }

    suspend fun getRecentSessions(): List<WorkoutSession> = sessionDao.getRecentSessions()

    suspend fun getLastLoadForExercise(exerciseId: Long): Float? =
        sessionDao.getLastSetForExercise(exerciseId)?.loadKg
}
