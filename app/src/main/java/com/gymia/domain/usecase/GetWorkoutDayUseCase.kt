package com.gymia.domain.usecase

import com.gymia.data.model.WorkoutDay
import com.gymia.data.repository.WorkoutRepository
import javax.inject.Inject

class GetWorkoutDayUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(dayId: Long): WorkoutDay? = repository.getDayById(dayId)
}
