package com.gymia.domain.model

data class DomainWorkoutPlan(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val source: String
)
