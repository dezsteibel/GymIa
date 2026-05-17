package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.DomainCardioRecord
import javax.inject.Inject

class DeleteCardioUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(record: DomainCardioRecord) = repository.deleteCardioRecord(record)
}
