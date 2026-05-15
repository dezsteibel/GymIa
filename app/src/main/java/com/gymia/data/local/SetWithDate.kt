package com.gymia.data.local

import androidx.room.ColumnInfo

data class SetWithDate(
    @ColumnInfo(name = "exerciseId") val exerciseId: Long,
    @ColumnInfo(name = "reps") val reps: Int,
    @ColumnInfo(name = "loadKg") val loadKg: Float,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "sessionId") val sessionId: Long
)
