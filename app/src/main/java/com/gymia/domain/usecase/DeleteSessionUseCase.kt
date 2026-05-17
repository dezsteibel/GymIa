package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import javax.inject.Inject

class DeleteSessionUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: Long) = repository.deleteSessionWithSets(sessionId)
}
