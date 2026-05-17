package com.gymia.data.local

data class SetWithExerciseName(
    val id: Long,
    val sessionId: Long,
    val exerciseId: Long,
    val setNumber: Int,
    val reps: Int,
    val loadKg: Float,
    val completed: Boolean,
    val exerciseName: String
)
