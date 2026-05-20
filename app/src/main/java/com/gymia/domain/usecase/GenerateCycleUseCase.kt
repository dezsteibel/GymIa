package com.gymia.domain.usecase

import com.gymia.data.repository.AiRepository
import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.DeloadReason
import com.gymia.domain.model.WorkoutCycle
import javax.inject.Inject

class GenerateCycleUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val aiRepository: AiRepository,
    private val detectDeloadNeedUseCase: DetectDeloadNeedUseCase
) {
    suspend operator fun invoke(): Result<WorkoutCycle> {
        val sessions = workoutRepository.getRecentSessions()
        val deload = runCatching { detectDeloadNeedUseCase() }.getOrNull()
        val deloadNote = buildDeloadNote(deload)
        return aiRepository.generateCycle(sessions, deloadNote)
    }

    private fun buildDeloadNote(suggestion: com.gymia.domain.model.DeloadSuggestion?): String? {
        if (suggestion?.shouldDeload != true) return null
        val exercises = suggestion.affectedExercises.joinToString(", ")
        val reason = when (suggestion.reason) {
            DeloadReason.STAGNATION -> "stagnation on $exercises"
            DeloadReason.REGRESSION -> "regression on $exercises"
            DeloadReason.HIGH_VOLUME -> "excessive volume spike"
            DeloadReason.NONE -> return null
        }
        return "Performance analysis indicates a deload may be needed due to $reason. " +
            "Please include a deload week at the start of the new cycle."
    }
}
