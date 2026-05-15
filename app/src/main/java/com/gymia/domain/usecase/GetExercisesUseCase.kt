package com.gymia.domain.usecase

import com.gymia.data.model.Exercise
import com.gymia.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExercisesUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(query: String = ""): Flow<List<Exercise>> =
        if (query.isBlank()) repository.getAllExercises()
        else repository.searchExercises(query)
}
