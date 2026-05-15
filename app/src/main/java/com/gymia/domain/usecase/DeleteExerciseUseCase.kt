package com.gymia.domain.usecase

import com.gymia.data.model.Exercise
import com.gymia.data.repository.WorkoutRepository
import javax.inject.Inject

class DeleteExerciseUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(exercise: Exercise) = repository.deleteExercise(exercise)
}
