package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import javax.inject.Inject

class DeleteWorkoutPlanUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(planId: Long) = repository.deletePlanWithCascade(planId)
}
