package com.gymia.domain.usecase

import com.gymia.data.model.SetRecord
import com.gymia.data.model.WorkoutSession
import com.gymia.data.repository.WorkoutRepository
import javax.inject.Inject

class LogSessionUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(session: WorkoutSession, sets: List<SetRecord>) =
        repository.saveSession(session, sets)
}
