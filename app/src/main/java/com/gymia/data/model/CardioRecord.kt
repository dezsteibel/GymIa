package com.gymia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cardio_records")
data class CardioRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val activityType: String,
    val durationMinutes: Int,
    val distanceKm: Float? = null,
    val notes: String? = null
)
