package com.gymia.domain.model

data class PlanWithDays(
    val plan: DomainWorkoutPlan,
    val days: List<DomainWorkoutDay>
)
