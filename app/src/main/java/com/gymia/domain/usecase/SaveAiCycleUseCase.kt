package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.WorkoutCycle
import javax.inject.Inject

class SaveAiCycleUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(cycle: WorkoutCycle): Long =
        repository.saveAiCycle(cycle)
}
