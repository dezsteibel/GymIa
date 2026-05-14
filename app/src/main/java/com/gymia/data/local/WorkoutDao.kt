package com.gymia.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymia.data.model.WorkoutDay
import com.gymia.data.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: WorkoutPlan): Long

    @Update
    suspend fun updatePlan(plan: WorkoutPlan)

    @Delete
    suspend fun deletePlan(plan: WorkoutPlan)

    @Query("SELECT * FROM workout_plans ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<WorkoutPlan>>

    @Query("SELECT * FROM workout_plans WHERE id = :id")
    suspend fun getPlanById(id: Long): WorkoutPlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: WorkoutDay): Long

    @Update
    suspend fun updateDay(day: WorkoutDay)

    @Delete
    suspend fun deleteDay(day: WorkoutDay)

    @Query("SELECT * FROM workout_days WHERE planId = :planId ORDER BY `order` ASC")
    fun getDaysForPlan(planId: Long): Flow<List<WorkoutDay>>
}
