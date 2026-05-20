package com.gymia.domain.model

data class DomainSetWithExercise(
    val exerciseName: String,
    val reps: Int,
    val loadKg: Float,
    val sessionId: Long
)
