package com.gymia.domain.usecase

import com.gymia.data.model.WorkoutPlan
import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.DayExerciseInput
import com.gymia.domain.model.DayInput
import com.gymia.domain.model.TemplateDayInput
import com.gymia.domain.model.WorkoutTemplate
import javax.inject.Inject

class CreatePlanFromTemplateUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(template: WorkoutTemplate): Long {
        val days = template.days.map { day -> buildDayInput(day) }
        return workoutRepository.saveFullPlan(WorkoutPlan(name = template.name, source = "template"), days)
    }

    private suspend fun buildDayInput(day: TemplateDayInput): DayInput {
        val exercises = day.exercises.mapIndexed { index, ex ->
            val exerciseId = workoutRepository.findOrCreateExercise(ex.name, ex.muscleGroup, ex.equipmentType)
            DayExerciseInput(exerciseId = exerciseId, setsTarget = ex.setsTarget, order = index)
        }
        return DayInput(label = day.label, exercises = exercises)
    }
}
