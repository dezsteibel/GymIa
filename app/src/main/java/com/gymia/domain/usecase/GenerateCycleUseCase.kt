package com.gymia.domain.usecase

import com.gymia.data.repository.AiRepository
import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.WorkoutCycle
import javax.inject.Inject

class GenerateCycleUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val aiRepository: AiRepository
) {
    suspend operator fun invoke(): Result<WorkoutCycle> {
        val sessions = workoutRepository.getRecentSessions()
        val result = aiRepository.generateCycle(sessions)
        result.getOrNull()?.let { cycle -> workoutRepository.saveAiCycle(cycle) }
        return result
    }
}
