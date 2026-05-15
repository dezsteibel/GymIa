package com.gymia.domain.model

data class DomainWorkoutDay(
    val id: Long,
    val planId: Long,
    val label: String,
    val order: Int
)
