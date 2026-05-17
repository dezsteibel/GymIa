package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.DomainCardioRecord
import javax.inject.Inject

class UpdateCardioUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(record: DomainCardioRecord) = repository.updateCardioRecord(record)
}
