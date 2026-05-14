package com.gymia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long? = null,
    val dayId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 0,
    val notes: String? = null
)
