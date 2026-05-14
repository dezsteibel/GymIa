package com.gymia.data.repository

import com.gymia.data.local.ExerciseDao
import com.gymia.data.local.SessionDao
import com.gymia.data.local.WorkoutDao
import com.gymia.data.model.Exercise
import com.gymia.data.model.SetRecord
import com.gymia.data.model.WorkoutPlan
import com.gymia.data.model.WorkoutSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val sessionDao: SessionDao,
    private val exerciseDao: ExerciseDao
) {
    fun getAllPlans(): Flow<List<WorkoutPlan>> = workoutDao.getAllPlans()

    fun getAllSessions(): Flow<List<WorkoutSession>> = sessionDao.getAllSessions()

    fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAllExercises()

    fun getSetsForExercise(exerciseId: Long): Flow<List<SetRecord>> =
        sessionDao.getSetsForExercise(exerciseId)

    suspend fun saveSession(session: WorkoutSession, sets: List<SetRecord>) {
        val sessionId = sessionDao.insertSession(session)
        sessionDao.insertSets(sets.map { it.copy(sessionId = sessionId) })
    }

    suspend fun getRecentSessions(): List<WorkoutSession> = sessionDao.getRecentSessions()
}
