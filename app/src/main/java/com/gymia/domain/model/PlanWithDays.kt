package com.gymia.domain.model

import com.gymia.data.model.WorkoutDay
import com.gymia.data.model.WorkoutPlan

data class PlanWithDays(
    val plan: WorkoutPlan,
    val days: List<WorkoutDay>
)
