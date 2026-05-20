package com.gymia.domain.model

data class WarmUpSet(
    val setNumber: Int,
    val percentage: Float,
    val suggestedLoadKg: Float,
    val reps: Int,
    val completed: Boolean = false
)
