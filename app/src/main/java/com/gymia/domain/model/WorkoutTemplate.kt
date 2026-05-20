package com.gymia.domain.model

data class WorkoutTemplate(
    val id: String,
    val name: String,
    val description: String,
    val targetGoal: Goal,
    val daysPerWeek: Int,
    val days: List<TemplateDayInput>
)

data class TemplateDayInput(
    val label: String,
    val exercises: List<TemplateExerciseInput>
)

data class TemplateExerciseInput(
    val name: String,
    val muscleGroup: String,
    val equipmentType: String,
    val setsTarget: Int
)
