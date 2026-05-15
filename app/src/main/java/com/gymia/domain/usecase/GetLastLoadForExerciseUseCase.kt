package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import javax.inject.Inject

class GetLastLoadForExerciseUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(exerciseId: Long): Float? =
        repository.getLastLoadForExercise(exerciseId)
}
