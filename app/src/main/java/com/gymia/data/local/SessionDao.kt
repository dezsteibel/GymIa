package com.gymia.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymia.data.model.SetRecord
import com.gymia.data.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession): Long

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Delete
    suspend fun deleteSession(session: WorkoutSession)

    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int = 50): List<WorkoutSession>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): WorkoutSession?

    @Query("""
        SELECT workout_sessions.*, COUNT(set_records.id) as setCount, workout_plans.name as planName
        FROM workout_sessions
        LEFT JOIN set_records ON set_records.sessionId = workout_sessions.id
        LEFT JOIN workout_plans ON workout_plans.id = workout_sessions.planId
        GROUP BY workout_sessions.id
        ORDER BY workout_sessions.date DESC
    """)
    fun getSessionsWithSetCount(): Flow<List<SessionWithCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<SetRecord>)

    @Query("SELECT * FROM set_records WHERE sessionId = :sessionId ORDER BY setNumber ASC")
    fun getSetsForSession(sessionId: Long): Flow<List<SetRecord>>

    @Query("SELECT * FROM set_records WHERE exerciseId = :exerciseId ORDER BY sessionId DESC")
    fun getSetsForExercise(exerciseId: Long): Flow<List<SetRecord>>

    @Query("SELECT * FROM set_records WHERE exerciseId = :exerciseId ORDER BY id DESC LIMIT 1")
    suspend fun getLastSetForExercise(exerciseId: Long): SetRecord?

    @Query("""
        SELECT sr.exerciseId, sr.reps, sr.loadKg, ws.date, sr.sessionId
        FROM set_records sr
        INNER JOIN workout_sessions ws ON ws.id = sr.sessionId
        ORDER BY ws.date ASC
    """)
    fun getAllSetsWithDates(): Flow<List<SetWithDate>>

    @Query("""
        SELECT sr.exerciseId, sr.reps, sr.loadKg, ws.date, sr.sessionId
        FROM set_records sr
        INNER JOIN workout_sessions ws ON ws.id = sr.sessionId
        WHERE sr.exerciseId = :exerciseId
        ORDER BY ws.date ASC
    """)
    fun getLoadTrendForExercise(exerciseId: Long): Flow<List<SetWithDate>>

    @Query("DELETE FROM set_records WHERE sessionId = :sessionId")
    suspend fun deleteSetsForSession(sessionId: Long)

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Query("""
        SELECT sr.id, sr.sessionId, sr.exerciseId, sr.setNumber, sr.reps, sr.loadKg, sr.completed,
               e.name as exerciseName, sr.notes
        FROM set_records sr
        INNER JOIN exercises e ON e.id = sr.exerciseId
        WHERE sr.sessionId = :sessionId
        ORDER BY sr.exerciseId ASC, sr.setNumber ASC
    """)
    suspend fun getSetsWithExerciseNames(sessionId: Long): List<SetWithExerciseName>

    @Query("SELECT * FROM workout_sessions ORDER BY date ASC")
    suspend fun getAllSessionsOnce(): List<WorkoutSession>

    @Query("SELECT * FROM set_records ORDER BY id ASC")
    suspend fun getAllSetsOnce(): List<SetRecord>

    @Query("""
        SELECT sr.id, sr.sessionId, sr.exerciseId, sr.setNumber, sr.reps, sr.loadKg, sr.completed,
               e.name as exerciseName, sr.notes
        FROM set_records sr
        INNER JOIN workout_sessions ws ON ws.id = sr.sessionId
        INNER JOIN exercises e ON e.id = sr.exerciseId
        ORDER BY ws.date ASC
    """)
    suspend fun getAllSetsWithNamesOnce(): List<SetWithExerciseName>
}
