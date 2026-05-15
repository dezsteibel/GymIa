package com.gymia.data.local

import androidx.room.Embedded
import androidx.room.Relation
import com.gymia.data.model.WorkoutDay
import com.gymia.data.model.WorkoutPlan

data class PlanWithDaysEntity(
    @Embedded val plan: WorkoutPlan,
    @Relation(parentColumn = "id", entityColumn = "planId")
    val days: List<WorkoutDay>
)
