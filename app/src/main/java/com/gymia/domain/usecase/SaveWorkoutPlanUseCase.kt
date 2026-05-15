package com.gymia.domain.usecase

import com.gymia.data.model.WorkoutPlan
import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.DayInput
import javax.inject.Inject

class SaveWorkoutPlanUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(plan: WorkoutPlan, days: List<DayInput>): Long =
        repository.saveFullPlan(plan, days)
}
