package com.gymia.domain.usecase

import com.gymia.data.model.SetRecord
import com.gymia.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProgressUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(exerciseId: Long): Flow<List<SetRecord>> =
        repository.getSetsForExercise(exerciseId)
}
