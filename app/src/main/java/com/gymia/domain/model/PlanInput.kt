package com.gymia.domain.model

data class DayExerciseInput(val exerciseId: Long, val setsTarget: Int, val order: Int)

data class DayInput(val label: String, val exercises: List<DayExerciseInput>)
