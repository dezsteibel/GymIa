package com.gymia.domain.usecase

import com.gymia.data.model.CardioRecord
import com.gymia.data.repository.WorkoutRepository
import javax.inject.Inject

class SaveCardioUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(record: CardioRecord): Long = repository.saveCardioRecord(record)
}
