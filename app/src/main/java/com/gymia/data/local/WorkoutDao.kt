package com.gymia.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymia.data.model.DayExercise
import com.gymia.data.model.WorkoutDay
import com.gymia.data.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Transaction
    @Query("SELECT * FROM workout_plans ORDER BY createdAt DESC")
    fun getPlansWithDays(): Flow<List<PlanWithDaysEntity>>

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

    @Query("SELECT * FROM workout_days WHERE id = :dayId")
    suspend fun getDayById(dayId: Long): WorkoutDay?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayExercise(dayExercise: DayExercise): Long

    @Delete
    suspend fun deleteDayExercise(dayExercise: DayExercise)

    @Query("DELETE FROM day_exercises WHERE dayId = :dayId")
    suspend fun clearDayExercises(dayId: Long)

    @Transaction
    @Query("SELECT * FROM day_exercises WHERE dayId = :dayId ORDER BY `order` ASC")
    fun getExercisesWithDetailForDay(dayId: Long): Flow<List<DayExerciseWithDetail>>
}
