package com.gymia.domain.usecase

import com.gymia.data.repository.AiRepository
import com.gymia.data.repository.WorkoutRepository
import javax.inject.Inject

class GenerateCycleUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val aiRepository: AiRepository
) {
    suspend operator fun invoke(): Result<String> {
        val sessions = workoutRepository.getRecentSessions()
        return aiRepository.generateCycle(sessions)
    }
}
