package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.PlanWithDays
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWorkoutPlansUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<PlanWithDays>> = repository.getPlansWithDays()
}
