package com.gymia.domain.usecase

import com.gymia.data.model.WorkoutPlan
import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.DayExerciseInput
import com.gymia.domain.model.DayInput
import javax.inject.Inject

class DuplicatePlanUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(planId: Long): Long {
        val planWithDays = repository.getPlanWithDaysOnce(planId)
            ?: error("Plan not found: $planId")
        val dayInputs = planWithDays.days.map { day ->
            val exercises = repository.getDayExercisesOnce(day.id)
            DayInput(
                label = day.label,
                exercises = exercises.mapIndexed { index, ef ->
                    DayExerciseInput(exerciseId = ef.exercise.id, setsTarget = ef.setsTarget, order = index)
                }
            )
        }
        val newPlan = WorkoutPlan(name = "${planWithDays.plan.name} (copy)", source = planWithDays.plan.source)
        return repository.saveFullPlan(newPlan, dayInputs)
    }
}
