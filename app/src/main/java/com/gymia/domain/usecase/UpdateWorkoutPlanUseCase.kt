package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.DayInput
import javax.inject.Inject

class UpdateWorkoutPlanUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(planId: Long, name: String, days: List<DayInput>) =
        repository.updateFullPlan(planId, name, days)
}
