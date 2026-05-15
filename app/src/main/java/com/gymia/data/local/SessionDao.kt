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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<SetRecord>)

    @Query("SELECT * FROM set_records WHERE sessionId = :sessionId ORDER BY setNumber ASC")
    fun getSetsForSession(sessionId: Long): Flow<List<SetRecord>>

    @Query("SELECT * FROM set_records WHERE exerciseId = :exerciseId ORDER BY sessionId DESC")
    fun getSetsForExercise(exerciseId: Long): Flow<List<SetRecord>>
}
