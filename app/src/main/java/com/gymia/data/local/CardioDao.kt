package com.gymia.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymia.data.model.CardioRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface CardioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: CardioRecord): Long

    @Update
    suspend fun update(record: CardioRecord)

    @Delete
    suspend fun delete(record: CardioRecord)

    @Query("SELECT * FROM cardio_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<CardioRecord>>

    @Query("SELECT * FROM cardio_records WHERE id = :id")
    suspend fun getById(id: Long): CardioRecord?
}
