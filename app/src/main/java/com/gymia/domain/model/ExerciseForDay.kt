package com.gymia.domain.model

import com.gymia.data.model.Exercise

data class ExerciseForDay(
    val dayExerciseId: Long,
    val exercise: Exercise,
    val setsTarget: Int,
    val order: Int
)
