package com.gymia.data.local

import androidx.room.Embedded
import com.gymia.data.model.WorkoutSession

data class SessionWithCount(
    @Embedded val session: WorkoutSession,
    val setCount: Int,
    val planName: String?
)
