package com.gymia.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymia.data.model.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: Exercise)

    @Update
    suspend fun update(exercise: Exercise)

    @Delete
    suspend fun delete(exercise: Exercise)

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): Exercise?

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<Exercise>>
}
