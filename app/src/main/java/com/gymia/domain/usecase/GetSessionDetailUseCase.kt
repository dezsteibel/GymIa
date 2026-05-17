package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.SessionDetail
import javax.inject.Inject

class GetSessionDetailUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: Long): SessionDetail? = repository.getSessionDetail(sessionId)
}
