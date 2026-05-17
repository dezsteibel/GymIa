package com.gymia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "set_records")
data class SetRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val setNumber: Int,
    val reps: Int,
    val loadKg: Float,
    val completed: Boolean = true,
    val notes: String? = null
)
