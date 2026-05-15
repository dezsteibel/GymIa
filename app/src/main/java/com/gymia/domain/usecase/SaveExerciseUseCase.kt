package com.gymia.domain.usecase

import com.gymia.data.model.Exercise
import com.gymia.data.repository.WorkoutRepository
import javax.inject.Inject

class SaveExerciseUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(exercise: Exercise) = repository.saveExercise(exercise)
}
