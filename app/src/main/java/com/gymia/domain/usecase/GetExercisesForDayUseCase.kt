package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.ExerciseForDay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExercisesForDayUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(dayId: Long): Flow<List<ExerciseForDay>> =
        repository.getExercisesForDay(dayId)
}
