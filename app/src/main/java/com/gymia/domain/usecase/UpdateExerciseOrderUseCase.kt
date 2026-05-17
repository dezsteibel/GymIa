package com.gymia.domain.usecase

import com.gymia.data.model.DayExercise
import com.gymia.data.repository.WorkoutRepository
import javax.inject.Inject

class UpdateExerciseOrderUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(exercises: List<DayExercise>) =
        repository.upsertExerciseOrder(exercises)
}
