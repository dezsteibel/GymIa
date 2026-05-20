package com.gymia.domain.usecase

import com.gymia.data.repository.AiRepository
import com.gymia.data.repository.WorkoutRepository
import com.gymia.domain.model.DeloadReason
import com.gymia.domain.model.DeloadSuggestion
import com.gymia.domain.model.WorkoutCycle
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GenerateCycleUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val aiRepository: AiRepository,
    private val getUserProfile: GetUserProfileUseCase,
    private val detectDeloadNeed: DetectDeloadNeedUseCase
) {
    suspend operator fun invoke(): Result<WorkoutCycle> {
        val sessions = workoutRepository.getRecentSessions()
        val profile = getUserProfile().first()
        val deload = detectDeloadNeed()
        val deloadNote = if (deload.shouldDeload) buildDeloadNote(deload) else null
        return aiRepository.generateCycle(sessions, profile, deloadNote)
    }

    private fun buildDeloadNote(suggestion: DeloadSuggestion): String? {
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
