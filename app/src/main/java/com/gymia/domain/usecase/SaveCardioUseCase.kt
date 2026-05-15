package com.gymia.domain.usecase

import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.DomainCardioRecord
import javax.inject.Inject

class SaveCardioUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(record: DomainCardioRecord): Long = repository.saveCardioRecord(record)
}
