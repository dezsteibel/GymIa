package com.gymia.domain.model

data class ExerciseForDay(
    val dayExerciseId: Long,
    val exercise: DomainExercise,
    val setsTarget: Int,
    val order: Int
)
