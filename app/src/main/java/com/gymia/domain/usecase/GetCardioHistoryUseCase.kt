package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.DomainCardioRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCardioHistoryUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<DomainCardioRecord>> = repository.getCardioHistory()
}
