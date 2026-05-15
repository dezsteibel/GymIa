package com.gymia.domain.usecase

import com.gymia.data.model.WorkoutSession
import com.gymia.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllSessionsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<WorkoutSession>> = repository.getAllSessions()
}
