package com.gymia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_days")
data class WorkoutDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val label: String,
    val order: Int
)
