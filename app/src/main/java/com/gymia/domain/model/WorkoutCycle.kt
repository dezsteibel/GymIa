package com.gymia.domain.model

data class WorkoutCycle(
    val cycleName: String,
    val durationWeeks: Int,
    val generalNotes: String,
    val days: List<CycleDay>
)

data class CycleDay(
    val dayLabel: String,
    val exercises: List<CycleExercise>
)

data class CycleExercise(
    val name: String,
    val sets: Int,
    val repsTarget: String,
    val loadSuggestionKg: Double,
    val progressionNote: String
)
