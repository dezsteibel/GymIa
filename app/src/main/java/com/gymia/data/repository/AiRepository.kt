package com.gymia.data.repository

import com.gymia.data.model.WorkoutSession
import com.gymia.data.remote.AnthropicApi
import com.gymia.data.remote.dto.AiMessage
import com.gymia.data.remote.dto.AiRequest
import com.gymia.data.remote.dto.WorkoutCycleDto
import com.gymia.data.remote.dto.toDomain
import com.gymia.domain.model.ExperienceLevel
import com.gymia.domain.model.Goal
import com.gymia.domain.model.UserProfile
import com.gymia.domain.model.WorkoutCycle
import com.gymia.domain.usecase.GetUserProfileUseCase
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AiRepository @Inject constructor(
    private val anthropicApi: AnthropicApi,
    private val json: Json,
    private val getUserProfile: GetUserProfileUseCase
) {
    suspend fun generateCycle(
        sessions: List<WorkoutSession>,
        deloadNote: String? = null
    ): Result<WorkoutCycle> = runCatching {
        val profile = getUserProfile().first()
        val request = AiRequest(
            system = buildSystemPrompt(profile),
            messages = listOf(AiMessage(role = "user", content = buildUserMessage(sessions, deloadNote)))
        )
        val responseText = anthropicApi.generateMessage(request).content.first().text
        json.decodeFromString<WorkoutCycleDto>(responseText).toDomain()
    }

    private fun buildSystemPrompt(profile: UserProfile): String {
        val goalText = when (profile.goal) {
            Goal.STRENGTH -> "strength"
            Goal.HYPERTROPHY -> "hypertrophy"
            Goal.MIXED -> "strength and hypertrophy (mixed)"
            Goal.FAT_LOSS -> "fat loss with muscle retention"
        }
        val levelText = when (profile.experienceLevel) {
            ExperienceLevel.BEGINNER -> "beginner (< 1 year)"
            ExperienceLevel.INTERMEDIATE -> "intermediate (1–3 years)"
            ExperienceLevel.ADVANCED -> "advanced (3+ years)"
        }
        val nameClause = if (profile.name.isNotBlank()) "The user's name is ${profile.name}. " else ""
        val weightClause = profile.bodyWeightKg?.let { " Body weight: ${it}kg." } ?: ""
        val notesClause = profile.notes?.takeIf { it.isNotBlank() }?.let { " Additional notes: $it" } ?: ""
        return "You are an expert strength and conditioning coach specializing in periodized training.\n" +
            "${nameClause}Goal: $goalText. Level: $levelText. " +
            "Training ${profile.trainingDaysPerWeek} days/week.$weightClause$notesClause\n\n" +
            "When asked to generate a training cycle, analyze the provided workout history and " +
            "respond ONLY with a valid JSON object. Do not include markdown, code blocks, or any " +
            "explanation outside the JSON.\n\n" +
            "The JSON must follow this exact structure:\n" +
            "{\n  \"cycle_name\": \"string\",\n  \"duration_weeks\": number,\n  \"general_notes\": \"string\",\n" +
            "  \"days\": [\n    {\n      \"day_label\": \"string\",\n      \"exercises\": [\n" +
            "        {\n          \"name\": \"string\",\n          \"sets\": number,\n" +
            "          \"reps_target\": \"string\",\n          \"load_suggestion_kg\": number,\n" +
            "          \"progression_note\": \"string\"\n        }\n      ]\n    }\n  ]\n}"
    }

    private fun buildUserMessage(sessions: List<WorkoutSession>, deloadNote: String?): String {
        val deloadSection = if (deloadNote != null) "\n\n$deloadNote" else ""
        return """
            Here is my recent workout history:
            ${json.encodeToString(sessions)}

            Please generate my next periodized training cycle based on this data.
            Consider my performance trends, stagnation points, and progression opportunities.
            Respond only with the JSON object as specified.$deloadSection
        """.trimIndent()
    }
}
